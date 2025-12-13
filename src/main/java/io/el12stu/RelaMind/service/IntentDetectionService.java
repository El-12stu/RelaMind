package io.el12stu.RelaMind.service;

import io.el12stu.RelaMind.service.IntentDetectionResult.ChatIntent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;
import java.util.Arrays;
import java.util.List;

/**
 * 意图识别服务
 * 支持基于规则和基于 LLM 的意图识别
 */
@Service
@Slf4j
public class IntentDetectionService {

    private final ChatClient intentDetectionClient;
    
    // 置信度阈值：低于此值需要询问用户确认
    private static final double CONFIDENCE_THRESHOLD = 0.7;
    
    // 是否使用 LLM 进行意图识别（默认使用规则，可切换为 LLM）
    private static final boolean USE_LLM_DETECTION = false;

    /**
     * 意图识别结果的结构化输出（用于 LLM 分类）
     */
    public record IntentClassification(
        /**
         * 意图类型：NORMAL_CHAT（普通聊天）、RAG_QUERY（历史查询）、TOOL_CALL（工具调用）
         */
        String intent,
        
        /**
         * 置信度：0.0-1.0，表示对识别结果的信心程度
         */
        Double confidence,
        
        /**
         * 识别理由：简要说明为什么识别为这个意图
         */
        String reason
    ) {}

    // 历史查询关键词模式
    private static final List<Pattern> HISTORY_QUERY_PATTERNS = Arrays.asList(
        // 时间相关查询
        Pattern.compile(".*(之前|以前|过去|去年|前年|上个月|上周|昨天|前天|几.*年前|几.*个月前).*"),
        Pattern.compile(".*(什么时候|何时|哪天|哪一天).*"),
        Pattern.compile(".*(.*年前|.*个月前|.*周前|.*天前).*"),
        Pattern.compile(".*(.*年.*月.*日|.*月.*日).*"),
        
        // 历史记录查询
        Pattern.compile(".*(记录|日记|写过|记过|之前.*写过).*"),
        Pattern.compile(".*(回顾|回忆|回想|想起|记得).*"),
        Pattern.compile(".*(之前.*做过|以前.*做过|过去.*做过).*"),
        
        // 模式分析查询
        Pattern.compile(".*(分析|统计|总结|模式|规律|趋势).*"),
        Pattern.compile(".*(变化|对比|比较|差异).*"),
        Pattern.compile(".*(经常|总是|通常|习惯).*"),
        
        // 相似经历查询
        Pattern.compile(".*(类似|相似|一样|同样|遇到过).*"),
        Pattern.compile(".*(之前.*怎么|以前.*怎么|过去.*怎么).*"),
        Pattern.compile(".*(怎么解决|如何处理|怎么办).*"),
        
        // 直接查询历史
        Pattern.compile(".*(查找|搜索|检索|查询).*历史.*"),
        Pattern.compile(".*(我的.*历史|我的.*记录|我的.*日记).*")
    );

    // 工具调用关键词模式
    private static final List<Pattern> TOOL_QUERY_PATTERNS = Arrays.asList(
        Pattern.compile(".*(搜索|查找|找|下载|保存|生成|执行|运行).*"),
        Pattern.compile(".*(网页|网站|链接|URL).*"),
        Pattern.compile(".*(文件|文档|PDF|图片).*"),
        Pattern.compile(".*(代码|脚本|命令).*")
    );

    public IntentDetectionService(ChatModel dashscopeChatModel) {
        if (dashscopeChatModel == null) {
            throw new IllegalArgumentException("ChatModel cannot be null");
        }
        
        String intentDetectionPrompt = """
            你是一个专业的用户意图分类器。根据用户消息，判断用户的真实意图。
            
            意图类型说明：
            1. NORMAL_CHAT：普通聊天、日常对话、情感陪伴、倾诉、记录生活等
            2. RAG_QUERY：查询历史记录、询问过去的事情、分析历史模式、回顾日记等
            3. TOOL_CALL：需要调用外部工具，如搜索、下载、生成文件、执行代码等
            
            判断规则：
            - 如果用户询问"之前"、"过去"、"去年"、"什么时候"等时间相关，或询问"记录"、"日记"、"历史"，优先判断为 RAG_QUERY
            - 如果用户明确要求"搜索"、"下载"、"生成"、"执行"等操作，判断为 TOOL_CALL
            - 如果只是日常聊天、倾诉、记录当前状态，判断为 NORMAL_CHAT
            - 如果意图不明确，置信度应该降低
            
            请仔细分析用户消息，给出准确的意图分类、置信度（0.0-1.0）和理由。
            """;
        
        this.intentDetectionClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(intentDetectionPrompt)
                .build();
    }

    /**
     * 检测用户意图
     * 优先使用规则匹配（快速），可选使用 LLM 分类（准确）
     */
    public IntentDetectionResult detectIntent(String message) {
        if (message == null || message.trim().isEmpty()) {
            return new IntentDetectionResult(
                ChatIntent.NORMAL_CHAT, 
                1.0, 
                false, 
                "空消息默认为普通聊天"
            );
        }

        if (USE_LLM_DETECTION) {
            return detectIntentByLLM(message);
        } else {
            return detectIntentByRule(message);
        }
    }

    /**
     * 基于规则的意图识别（快速）
     */
    private IntentDetectionResult detectIntentByRule(String message) {
        String lowerMessage = message.toLowerCase();

        // 优先检测是否需要工具调用
        if (matchesPattern(lowerMessage, TOOL_QUERY_PATTERNS)) {
            return new IntentDetectionResult(
                ChatIntent.TOOL_CALL,
                0.85,
                false,
                "检测到工具调用关键词"
            );
        }

        // 检测是否需要 RAG 检索
        if (matchesPattern(lowerMessage, HISTORY_QUERY_PATTERNS)) {
            return new IntentDetectionResult(
                ChatIntent.RAG_QUERY,
                0.80,
                false,
                "检测到历史查询关键词"
            );
        }

        // 默认普通聊天
        return new IntentDetectionResult(
            ChatIntent.NORMAL_CHAT,
            0.90,
            false,
            "未匹配到特殊意图，默认为普通聊天"
        );
    }

    /**
     * 基于 LLM 的意图识别（准确）
     */
    private IntentDetectionResult detectIntentByLLM(String message) {
        try {
            // 使用 LLM 进行意图分类（结构化输出）
            IntentClassification classification = intentDetectionClient
                    .prompt()
                    .user("请分析以下用户消息的意图：\n" + message)
                    .call()
                    .entity(IntentClassification.class);

            if (classification == null) {
                log.warn("LLM 返回的意图分类为 null，降级为规则匹配");
                return detectIntentByRule(message);
            }

            // 解析意图类型
            ChatIntent intent = parseIntent(classification.intent());
            double confidence = classification.confidence() != null 
                ? Math.max(0.0, Math.min(1.0, classification.confidence())) 
                : 0.5; // 默认置信度

            boolean needConfirmation = confidence < CONFIDENCE_THRESHOLD;
            String reason = classification.reason() != null ? classification.reason() : "LLM 识别";

            log.info("LLM 意图识别结果: intent={}, confidence={}, reason={}", 
                intent, confidence, reason);

            return new IntentDetectionResult(
                intent,
                confidence,
                needConfirmation,
                reason
            );
        } catch (Exception e) {
            log.error("LLM 意图识别失败，降级为规则匹配", e);
            // 降级为规则匹配
            return detectIntentByRule(message);
        }
    }

    /**
     * 解析意图字符串为枚举
     */
    private ChatIntent parseIntent(String intentStr) {
        if (intentStr == null) {
            return ChatIntent.NORMAL_CHAT;
        }
        
        String upper = intentStr.toUpperCase().trim();
        try {
            return ChatIntent.valueOf(upper);
        } catch (IllegalArgumentException e) {
            log.warn("未知的意图类型: {}, 默认为 NORMAL_CHAT", intentStr);
            return ChatIntent.NORMAL_CHAT;
        }
    }

    /**
     * 检查消息是否匹配模式列表
     */
    private boolean matchesPattern(String message, List<Pattern> patterns) {
        return patterns.stream().anyMatch(pattern -> pattern.matcher(message).matches());
    }
}

