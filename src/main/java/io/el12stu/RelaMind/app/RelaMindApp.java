package io.el12stu.RelaMind.app;

import io.el12stu.RelaMind.advisor.MyLoggerAdvisor;
import io.el12stu.RelaMind.advisor.ReReadingAdvisor;
import io.el12stu.RelaMind.advisor.SensitiveWordAdvisor;
import io.el12stu.RelaMind.advisor.ToolCallLimitAdvisor;
import io.el12stu.RelaMind.chatmemory.CassandraBasedChatMemory;
import io.el12stu.RelaMind.rag.QueryRewriter;
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



    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";

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
                .content();
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

    // 基于内存的向量数据库，已注释bean，按需开启
    @Autowired(required = false)
    private VectorStore RelaMindappvectorstore;
    // 基于云知识库服务的 RAG 应用 Advisor
    @Autowired(required = false)
    private Advisor RelaMindAppRagCloudAdvisor;
    // 基于 PgVector 向量存储的 RAG 应用 Advisor
    @Autowired(required = false)
    private VectorStore pgVectorVectorStore;
    // 重写查询服务
    @Resource
    private QueryRewriter queryRewriter;

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
                // 使用改写后的查询
                .user(rewrittenMessage)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                // RAG应用(基于内存的向量数据库)
                .advisors(new QuestionAnswerAdvisor(RelaMindappvectorstore))
                // 应用 RAG 检索增强服务（基于云知识库服务）
                //.advisors(RelaMindAppRagCloudAdvisor)
                // 应用 RAG 检索增强服务（基于 PgVector 向量存储）
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
        log.info("content: {}", content);
        return content;
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
                .toolCallbacks(toolCallbackProvider)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }
}
