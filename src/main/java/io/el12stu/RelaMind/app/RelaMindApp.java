package io.el12stu.RelaMind.app;

import io.el12stu.RelaMind.advisor.MyLoggerAdvisor;
import io.el12stu.RelaMind.advisor.ReReadingAdvisor;
import io.el12stu.RelaMind.advisor.SensitiveWordAdvisor;
import io.el12stu.RelaMind.advisor.ToolCallLimitAdvisor;
import io.el12stu.RelaMind.chatmemory.CassandraBasedChatMemory;
import io.el12stu.RelaMind.rag.QueryRewriter;
import io.el12stu.RelaMind.service.IntentDetectionService;
import io.el12stu.RelaMind.service.IntentDetectionResult;
import io.el12stu.RelaMind.service.IntentDetectionResult.ChatIntent;
import io.el12stu.RelaMind.service.SensitiveWordService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;



@Component
@Slf4j
public class RelaMindApp {

    private final ChatClient chatClient;



    // 普通聊天的 System Prompt - 定位为成长伙伴
    private static final String SYSTEM_PROMPT = 
        "你是用户的AI成长伙伴，名叫RelaMind。你的使命是帮助用户记录成长、理解自己、成为更好的自己。\n\n" +
        "核心职责：\n" +
        "1. **日常陪伴**：倾听用户的心声，给予情感支持和理解\n" +
        "2. **引导记录**：鼓励用户记录生活点滴，帮助用户养成记录习惯\n" +
        "3. **成长分析**：当用户询问历史或模式时，主动检索相关记录并给出洞察\n" +
        "4. **主动关怀**：记住用户的重要信息（如目标、困扰、重要日期），适时关心\n\n" +
        "对话风格：\n" +
        "- 温暖、真诚、有洞察力，像一位了解用户的老朋友\n" +
        "- 不要过于正式，用自然、亲切的语气交流\n" +
        "- 善于提问，引导用户深入思考\n" +
        "- 当用户分享经历时，可以建议记录下来，方便以后回顾\n\n" +
        "特殊能力：\n" +
        "- 你可以访问用户的历史记录（日记、笔记等），当用户询问过去的事情时，主动检索相关记录\n" +
        "- 你可以识别用户的成长模式，帮助用户理解自己的行为规律\n" +
        "- 你可以基于历史数据给出个性化建议，而不是泛泛而谈\n\n" +
        "开场：初次见面时，简单介绍自己，询问用户今天想聊什么，或者是否想记录些什么。";

    // RAG 专用的 System Prompt（更聚焦于历史分析）
    private static final String RAG_SYSTEM_PROMPT = 
        "你是用户的AI成长伙伴，正在基于用户的历史记录回答问题。\n\n" +
        "你的任务：\n" +
        "1. 仔细分析检索到的历史记录，找出与用户问题相关的信息\n" +
        "2. 如果找到相关信息，用具体的时间、事件来回答，让用户感受到你真正了解他的过去\n" +
        "3. 如果信息不足，诚实地告诉用户，并建议他记录更多相关内容\n" +
        "4. 尝试发现模式：如果用户多次遇到类似情况，可以指出这个模式\n" +
        "5. 给出基于历史的建议：告诉用户之前是怎么处理的，效果如何\n\n" +
        "回答风格：\n" +
        "- 引用具体的时间、事件，让回答更有说服力\n" +
        "- 例如：'根据你2023年3月的记录，当时你...' 或 '我注意到你在过去一年中...'";

    /**
     * 初始化 ChatClient
     *
     * @param dashscopeChatModel
     */
    public RelaMindApp(ChatModel dashscopeChatModel,CassandraBasedChatMemory cassandraBasedChatMemory,SensitiveWordService sensitiveWordService) {
        // 使用基于 Cassandra 的对话记忆（替代文件存储）
        ChatMemory chatMemory = cassandraBasedChatMemory;
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        //敏感词拦截 Advisor，可按需开启
                        new SensitiveWordAdvisor(sensitiveWordService,dashscopeChatModel)
                        //工具调用上限工具，可按需开启，否则可能会无限调用工具
                        ,new ToolCallLimitAdvisor()
                        // 自定义日志 Advisor，可按需开启
                        ,new MyLoggerAdvisor()
//                     // 自定义推理增强 Advisor，可按需开启
                       //,new ReReadingAdvisor()
                )
                .build();
    }

    /**
     * AI 基础对话（支持多轮对话记忆）
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChat(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * AI 基础对话（支持多轮对话记忆，SSE 流式传输）
     *
     * @param message
     * @param chatId
     * @return
     */
    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content()
                .concatWith(Flux.just("[DONE]"));
    }

    record PsychReport(String title, List<String> suggestions) {

    }

    /**
     * AI 报告功能（实战结构化输出）
     *
     * @param message
     * @param chatId
     * @return
     */
    public PsychReport doChatWithReport(String message, String chatId) {
        PsychReport psychReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表")
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .entity(PsychReport.class);
        log.info("psychReport: {}", psychReport);
        return psychReport;
    }

    // RAG知识库问答功能

    // 基于内存的向量数据库
    @Autowired(required = false)
    private VectorStore RelaMindappvectorstore;

    // 基于云知识库服务的 RAG 应用 Advisor
    @Autowired(required = false)
    private Advisor RelaMindAppRagCloudAdvisor;

    // 基于(云数据库) PgVector 向量存储的 RAG 应用 Advisor
    @Autowired(required = false)
    private VectorStore pgVectorVectorStore;

    // 重写查询服务
    @Resource
    private QueryRewriter queryRewriter;

    // 意图识别服务
    @Resource
    private IntentDetectionService intentDetectionService;

    /**
     * 和 RAG 知识库进行对话
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithRag(String message, String chatId) {
        // 查询重写
        String rewrittenMessage = queryRewriter.doQueryRewrite(message);
        ChatResponse chatResponse = chatClient
                .prompt()
                .system(RAG_SYSTEM_PROMPT)  // 使用 RAG 专用 Prompt
                // 使用改写后的查询
                .user(rewrittenMessage)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                // RAG应用(基于内存的向量数据库)
                .advisors(new QuestionAnswerAdvisor(RelaMindappvectorstore))
                // 应用 RAG 检索增强服务（基于云知识库服务）。若开启，需要同步修改RelaMindAppRagCloudAdvisorConfig.java，并开启阿里云RAG服务
                //.advisors(RelaMindAppRagCloudAdvisor)
                // 应用 RAG 检索增强服务（阿里云 RDS PostgreSQL 向量数据库）。若开启，需要同步修改PgVectorVectorStoreConfig.java，并开启阿里云 RDS PostgreSQL数据库服务 以及 修改application.yml->Pgvectore
                //.advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))
                // 应用自定义的 RAG 检索增强服务（文档查询器 + 上下文增强器）
//                .advisors(
//                        RelaMindAppRagCustomAdvisorFactory.createRelaMindAppRagCustomAdvisor(
//                                RelaMindappvectorstore, "单身"
//                        )
//                )
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("RAG 回答: {}", content);
        return content;
    }

    /**
     * RAG 流式版本
     */
    public Flux<String> doChatWithRagByStream(String message, String chatId) {
        String rewrittenMessage = queryRewriter.doQueryRewrite(message);
        return chatClient
                .prompt()
                .system(RAG_SYSTEM_PROMPT)
                .user(rewrittenMessage)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(new QuestionAnswerAdvisor(RelaMindappvectorstore))
                .stream()
                .content()
                .concatWith(Flux.just("[DONE]"));
    }

    /**
     * 工具调用流式版本
     */
    public Flux<String> doChatWithToolsByStream(String message, String chatId) {
        return chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .toolCallbacks(allTools)
                .stream()
                .content()
                .concatWith(Flux.just("[DONE]"));
    }

    // AI 调用工具能力
    @Resource
    private ToolCallback[] allTools;

    /**
     * AI 功能（支持调用工具）
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithTools(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .toolCallbacks(allTools)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    // AI 调用 MCP 服务
    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    /**
     * AI 报告功能（调用 MCP 服务）
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithMcp(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                // AI 调用 MCP 服务,若开启，需要同步修改application.yml->MCP
                //.toolCallbacks(toolCallbackProvider)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * 智能聊天路由 - 根据用户意图自动选择最合适的聊天方式
     * 
     * @param message 用户消息
     * @param chatId 会话ID
     * @return AI回复
     */
    public String smartChat(String message, String chatId) {
        IntentDetectionResult intentResult = intentDetectionService.detectIntent(message);
        ChatIntent intent = intentResult.getIntent();
        double confidence = intentResult.getConfidence();
        boolean needConfirmation = intentResult.getNeedConfirmation();
        
        log.info("智能路由 - 意图: {}, 置信度: {}, 需要确认: {}, 理由: {}", 
            intent, confidence, needConfirmation, intentResult.getReason());

        // 如果置信度低，在回复中询问用户确认
        String confirmationPrompt = needConfirmation 
            ? "\n\n[系统提示：我对你的意图理解可能不够准确，如果这不是你想要的，请告诉我。]" 
            : "";

        String response = switch (intent) {
            case RAG_QUERY -> {
                // 使用 RAG 检索历史记录
                log.info("使用 RAG 模式回答");
                yield doChatWithRag(message, chatId);
            }
            case TOOL_CALL -> {
                // 使用工具调用
                log.info("使用工具调用模式");
                yield doChatWithTools(message, chatId);
            }
            case NORMAL_CHAT -> {
                // 普通聊天
                log.info("使用普通聊天模式");
                yield doChat(message, chatId);
            }
        };

        return response + confirmationPrompt;
    }

    /**
     * 智能聊天路由 - 流式版本
     */
    public Flux<String> smartChatByStream(String message, String chatId) {
        IntentDetectionResult intentResult = intentDetectionService.detectIntent(message);
        ChatIntent intent = intentResult.getIntent();
        double confidence = intentResult.getConfidence();
        boolean needConfirmation = intentResult.getNeedConfirmation();
        
        log.info("智能路由（流式） - 意图: {}, 置信度: {}, 需要确认: {}, 理由: {}", 
            intent, confidence, needConfirmation, intentResult.getReason());

        Flux<String> response = switch (intent) {
            case RAG_QUERY -> {
                // RAG 流式返回
                log.info("使用 RAG 模式回答（流式）");
                yield doChatWithRagByStream(message, chatId);
            }
            case TOOL_CALL -> {
                // 工具调用流式返回
                log.info("使用工具调用模式（流式）");
                yield doChatWithToolsByStream(message, chatId);
            }
            case NORMAL_CHAT -> {
                // 普通聊天流式返回
                log.info("使用普通聊天模式（流式）");
                yield doChatByStream(message, chatId);
            }
        };

        // 如果置信度低，在最后添加确认提示
        if (needConfirmation) {
            return response.concatWith(Flux.just("\n\n[系统提示：我对你的意图理解可能不够准确，如果这不是你想要的，请告诉我。]"));
        }
        
        return response;
    }
}
