package io.el12stu.RelaMind.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 意图识别结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntentDetectionResult {
    /**
     * 识别的意图类型
     */
    private ChatIntent intent;
    
    /**
     * 置信度 (0.0 - 1.0)
     */
    private Double confidence;
    
    /**
     * 是否需要询问用户确认（当置信度低时）
     */
    private Boolean needConfirmation;
    
    /**
     * 识别理由（用于调试和优化）
     */
    private String reason;
    
    /**
     * 聊天意图枚举
     */
    public enum ChatIntent {
        /**
         * 普通聊天 - 日常对话、情感陪伴
         */
        NORMAL_CHAT,
        
        /**
         * RAG 查询 - 需要检索历史记录
         */
        RAG_QUERY,
        
        /**
         * 工具调用 - 需要调用外部工具
         */
        TOOL_CALL
    }
}

