package io.el12stu.RelaMind.chatmemory;

import io.el12stu.RelaMind.chatmemory.entity.ChatMessageEntity;
import io.el12stu.RelaMind.chatmemory.repository.ChatMessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 基于 Cassandra 的聊天记忆存储实现
 * 优化：采用 Append-Only 模式，避免全量重写
 */
@Component
@Slf4j
public class CassandraBasedChatMemory implements ChatMemory {

    private final ChatMessageRepository chatMessageRepository;

    /**
     * 最大消息数量限制。
     * 如果设置为大于 0 的值，get 方法将只返回最新的 N 条消息。
     * 如果设置为 0 或负数，则不限制消息数量（返回所有消息）。
     */
    @Value("${relamind.chat.memory.max-messages:0}")
    private int maxMessages;

    public CassandraBasedChatMemory(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        // 关键修改：直接保存新消息，不需要读取旧消息，也不要删除旧消息
        // Cassandra 擅长写入，这里利用其高吞吐特性
        saveNewMessages(conversationId, messages);
    }

    @Override
    public List<Message> get(String conversationId) {
        // Eviction 策略：根据配置的 maxMessages 决定是否限制消息数量
        if (maxMessages > 0) {
            // 如果配置了最大消息数限制，只获取最新的 N 条消息
            return getLatestFromDatabase(conversationId, maxMessages);
        } else {
            // 如果没有限制（maxMessages <= 0），返回所有消息
            return getFromDatabase(conversationId);
        }
    }

    @Override
    public void clear(String conversationId) {
        try {
            // 注意：在 Cassandra 中频繁硬删除大量数据可能产生 Tombstones
            // 建议配合 TTL (Time To Live) 策略自动过期，而不是手动频繁 clear
            chatMessageRepository.deleteByKey(conversationId);
        } catch (Exception e) {
            log.error("Failed to clear conversation: {}", conversationId, e);
        }
    }

    private List<Message> getFromDatabase(String conversationId) {
        try {
            // 假设 Repository 按照 Cluster Key (timestamp) 升序排序
            List<ChatMessageEntity> entities = chatMessageRepository.findByKey(conversationId);
            List<Message> messages = new ArrayList<>();

            for (ChatMessageEntity entity : entities) {
                Message message = createMessageFromEntity(entity);
                if (message != null) {
                    messages.add(message);
                }
            }
            return messages;
        } catch (Exception e) {
            log.error("Failed to retrieve messages for conversation: {}", conversationId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 从数据库获取最新的 N 条消息。
     * 由于 CQL 查询返回的是降序排列（最新的在前），需要反转以保持时间正序。
     */
    private List<Message> getLatestFromDatabase(String conversationId, int limit) {
        try {
            // 使用优化查询获取最新的 N 条消息（降序）
            List<ChatMessageEntity> entities = chatMessageRepository.findLatestByKey(conversationId, limit);
            
            // 反转列表以保持时间升序（因为查询返回的是降序）
            // 这样保证返回的消息是按时间正序排列的，与 getFromDatabase 的行为一致
            Collections.reverse(entities);
            
            List<Message> messages = new ArrayList<>();
            for (ChatMessageEntity entity : entities) {
                Message message = createMessageFromEntity(entity);
                if (message != null) {
                    messages.add(message);
                }
            }
            
            log.debug("Retrieved latest {} messages from conversation: {}", messages.size(), conversationId);
            return messages;
        } catch (Exception e) {
            log.error("Failed to retrieve latest messages for conversation: {}", conversationId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 仅保存新消息
     */
    private void saveNewMessages(String conversationId, List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }

        try {
            long timestamp = System.currentTimeMillis();
            for (Message message : messages) {
                // 确保批量插入时时间戳不重复，保证排序
                saveMessage(conversationId, message, timestamp++);
            }
        } catch (Exception e) {
            log.error("Failed to save messages for conversation: {}", conversationId, e);
        }
    }

    private void saveMessage(String conversationId, Message message, long timestamp) {
        try {
            String content = message.getText();
            String messageType = getMessageType(message);

            // TODO: 如果需要支持 Function Call 或 Metadata，建议在这里将 message 序列化为 JSON 存入单独字段
            // String metadataJson = serializeMetadata(message.getMetadata());

            ChatMessageEntity entity = new ChatMessageEntity(
                    conversationId,
                    timestamp,
                    messageType,
                    content
            );

            chatMessageRepository.save(entity);
        } catch (Exception e) {
            log.error("Failed to save message: {}", message, e);
        }
    }

    private String getMessageType(Message message) {
        if (message instanceof UserMessage) return "USER";
        if (message instanceof SystemMessage) return "SYSTEM";
        if (message instanceof AssistantMessage) return "ASSISTANT";
        if (message instanceof ToolResponseMessage) return "TOOL"; // 增加了 Tool 支持
        return "UNKNOWN";
    }

    private Message createMessageFromEntity(ChatMessageEntity entity) {
        String type = entity.getType();
        String context = entity.getContext();

        // 简单的工厂模式
        return switch (type) {
            case "USER" -> new UserMessage(context);
            case "SYSTEM" -> new SystemMessage(context);
            case "ASSISTANT" -> new AssistantMessage(context);
            case "TOOL" -> new ToolResponseMessage(List.of(), Map.of()); // 仅作为示例，实际需要还原 Tool 输出
            default -> {
                log.warn("Unknown message type: {}, treating as USER", type);
                yield new UserMessage(context);
            }
        };
    }
}