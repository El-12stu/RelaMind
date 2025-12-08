package io.el12stu.RelaMind.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工具调用限制 Advisor
 * 限制每个会话最多调用6次工具，达到上限后让AI基于现有信息回答，不要抛异常中断
 * 通过检查 Prompt 历史消息中的 ToolMessage 数量来判断是否达到上限
 */
@Slf4j
public class ToolCallLimitAdvisor implements CallAdvisor, StreamAdvisor {

    /**
     * 工具调用上限
     */
    private static final int MAX_TOOL_CALLS = 6;

    /**
     * 系统提示消息（达到上限时注入）
     */
    private static final String LIMIT_REACHED_MESSAGE = 
            "系统提示:你已进行了足够的信息查询,现在必须基于已获取的信息给出回答,不要再调用任何工具,请立即给出最终答案。";

    /**
     * 线程安全的Map，用于追踪是否已经为该会话注入过限制消息
     * Key: 会话ID (chatId)
     * Value: 是否已注入过限制消息
     */
    private final ConcurrentHashMap<String, Boolean> messageInjectedMap = new ConcurrentHashMap<>();

    @Override
    @NonNull
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        // 设置为最高优先级，确保在其他 Advisor 之前执行
        return 0;
    }

    /**
     * 从 ChatClientRequest 中获取会话ID
     *
     * @param request ChatClientRequest
     * @return 会话ID，如果不存在则返回 null
     */
    @Nullable
    private String getConversationId(ChatClientRequest request) {
        Object conversationId = request.context().get(ChatMemory.CONVERSATION_ID);
        return conversationId != null ? conversationId.toString() : null;
    }

    /**
     * 统计 Prompt 消息列表中的 ToolResponseMessage 数量
     *
     * @param messages 消息列表
     * @return ToolMessage 的数量
     */
    private int countToolMessages(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (Message message : messages) {
            if (message instanceof ToolResponseMessage) {
                count++;
            }
        }
        return count;
    }

    /**
     * 处理请求前：检查是否达到上限，如果达到则注入系统消息
     * 通过统计 Prompt 历史消息中的 ToolMessage 数量来判断是否达到上限
     *
     * @param request ChatClientRequest
     * @return 处理后的 ChatClientRequest
     */
    @NonNull
    private ChatClientRequest before(@NonNull ChatClientRequest request) {
        String conversationId = getConversationId(request);
        
        try {
            // 获取当前 Prompt 的消息列表
            Prompt prompt = request.prompt();
            List<Message> messages = prompt.getInstructions();
            
            // 统计 ToolMessage 的数量
            int toolMessageCount = countToolMessages(messages);
            
            // 如果达到上限，注入限制消息
            if (toolMessageCount >= MAX_TOOL_CALLS) {
                // 检查是否已经注入过限制消息（避免重复注入）
                Boolean messageInjected = conversationId != null ? messageInjectedMap.get(conversationId) : null;
                if (!Boolean.TRUE.equals(messageInjected)) {
                    log.info("会话 {} 已达到工具调用上限 ({} 个 ToolMessage)，注入限制提示", 
                            conversationId != null ? conversationId : "未知", toolMessageCount);
                    List<Message> newMessages = new ArrayList<>(messages);
                    // 添加系统消息到消息列表的开头
                    newMessages.add(0, new SystemMessage(LIMIT_REACHED_MESSAGE));
                    Prompt newPrompt = new Prompt(newMessages);
                    // 标记已注入过消息
                    if (conversationId != null) {
                        messageInjectedMap.put(conversationId, true);
                    }
                    return new ChatClientRequest(newPrompt, request.context());
                }
                // 如果已经注入过消息，直接返回原请求（不再重复注入）
            } else {
                // 如果未达到上限，清除注入标记（允许在后续达到上限时再次注入）
                if (conversationId != null) {
                    messageInjectedMap.remove(conversationId);
                }
            }
        } catch (Exception e) {
            log.error("处理工具调用限制时发生错误", e);
        }

        return request;
    }

    /**
     * 处理响应后：清理注入标记
     * 仅在会话正常结束（finish_reason 为 "stop"）时清理 messageInjectedMap
     *
     * @param response ChatClientResponse
     * @param conversationId 会话ID
     */
    private void after(ChatClientResponse response, String conversationId) {
        if (conversationId == null) {
            return;
        }

        try {
            if (response == null) {
                return;
            }
            
            var chatResponse = response.chatResponse();
            if (chatResponse == null) {
                return;
            }
            
            Generation generation = chatResponse.getResult();
            if (generation == null) {
                return;
            }
            
            // 检查 finish_reason 是否为 "stop"，只有在这种情况下才清理注入标记
            String finishReason = null;
            try {
                if (generation.getMetadata() != null) {
                    Object finishReasonObj = generation.getMetadata().get("finish_reason");
                    if (finishReasonObj != null) {
                        finishReason = finishReasonObj.toString();
                    }
                }
            } catch (Exception e) {
                log.debug("无法获取 finish_reason", e);
            }
            
            // 只有当 finish_reason 是 "stop" 时才清理注入标记
            if ("stop".equalsIgnoreCase(finishReason)) {
                messageInjectedMap.remove(conversationId);
                log.info("会话 {} 正常结束，已清理注入标记", conversationId);
            }
        } catch (Exception e) {
            log.error("处理响应后清理时发生错误", e);
        }
    }

    @Override
    @NonNull
    public ChatClientResponse adviseCall(@NonNull ChatClientRequest chatClientRequest, @NonNull CallAdvisorChain chain) {
        String conversationId = getConversationId(chatClientRequest);
        
        // 处理请求前：检查并注入系统消息（如果需要）
        ChatClientRequest processedRequest = before(chatClientRequest);
        
        // 继续调用链
        ChatClientResponse response = chain.nextCall(processedRequest);
        
        // 处理响应后：清理注入标记（如果需要）
        after(response, conversationId);
        
        return response;
    }

    @Override
    @NonNull
    public Flux<ChatClientResponse> adviseStream(@NonNull ChatClientRequest chatClientRequest, @NonNull StreamAdvisorChain chain) {
        String conversationId = getConversationId(chatClientRequest);
        
        // 处理请求前：检查并注入系统消息（如果需要）
        ChatClientRequest processedRequest = before(chatClientRequest);
        
        // 继续调用链
        Flux<ChatClientResponse> responseFlux = chain.nextStream(processedRequest);
        
        // 使用 ChatClientMessageAggregator 聚合响应，以便在完成后处理
        return (new ChatClientMessageAggregator()).aggregateChatClientResponse(
                responseFlux,
                response -> after(response, conversationId)
        );
    }
}

