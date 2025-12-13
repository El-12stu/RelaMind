package io.el12stu.RelaMind.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 日记记录服务
 * 用于将用户的感想和心情存储到向量数据库（RAG）
 */
@Service
@Slf4j
public class DiaryService {

    @Autowired(required = false)
    private VectorStore RelaMindappvectorstore;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 保存日记记录到向量数据库
     * 
     * @param userId 用户ID（可选，用于多用户场景）
     * @param content 日记内容
     * @param mood 心情（可选）
     * @param tags 标签（可选）
     * @return 是否保存成功
     */
    public boolean saveDiary(String userId, String content, String mood, List<String> tags) {
        if (RelaMindappvectorstore == null) {
            log.error("向量数据库未初始化，无法保存日记");
            return false;
        }

        if (content == null || content.trim().isEmpty()) {
            log.warn("日记内容为空，无法保存");
            return false;
        }

        try {
            // 构建文档元数据
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("type", "diary");
            metadata.put("userId", userId != null ? userId : "default");
            metadata.put("timestamp", LocalDateTime.now().format(DATE_FORMATTER));
            metadata.put("date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            metadata.put("time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            
            if (mood != null && !mood.trim().isEmpty()) {
                metadata.put("mood", mood);
            }
            
            if (tags != null && !tags.isEmpty()) {
                metadata.put("tags", String.join(",", tags));
            }
            
            // 生成唯一ID
            String documentId = UUID.randomUUID().toString();
            metadata.put("documentId", documentId);

            // 构建完整的文档内容（包含元数据信息，便于检索）
            StringBuilder fullContent = new StringBuilder();
            Object dateObj = metadata.get("date");
            Object timeObj = metadata.get("time");
            String dateStr = dateObj != null ? String.valueOf(dateObj) : "";
            String timeStr = timeObj != null ? String.valueOf(timeObj) : "";
            fullContent.append("日期：").append(dateStr).append("\n");
            fullContent.append("时间：").append(timeStr).append("\n");
            if (mood != null && !mood.trim().isEmpty()) {
                fullContent.append("心情：").append(mood).append("\n");
            }
            if (tags != null && !tags.isEmpty()) {
                fullContent.append("标签：").append(String.join(",", tags)).append("\n");
            }
            fullContent.append("内容：").append(content);

            // 创建文档
            Document document = new Document(documentId, fullContent.toString(), metadata);

            // 存储到向量数据库
            @SuppressWarnings("null")
            List<Document> documents = List.of(document);
            RelaMindappvectorstore.add(documents);

            log.info("日记保存成功 - userId: {}, documentId: {}, date: {}", 
                userId, documentId, metadata.get("date"));
            
            return true;
        } catch (Exception e) {
            log.error("保存日记失败", e);
            return false;
        }
    }

    /**
     * 保存日记记录（简化版本，只需要内容）
     */
    public boolean saveDiary(String content) {
        return saveDiary(null, content, null, null);
    }

    /**
     * 保存日记记录（带用户ID和内容）
     */
    public boolean saveDiary(String userId, String content) {
        return saveDiary(userId, content, null, null);
    }
}

