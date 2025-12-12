package io.el12stu.RelaMind.agent;

import io.el12stu.RelaMind.advisor.MyLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

/**
 * AI 超级智能体（拥有自主规划能力，可以直接使用）
 */
@Component
public class Manus extends ToolCallAgent {

    public Manus(ToolCallback[] allTools, ChatModel dashscopeChatModel) {
        super(allTools);
        this.setName("Manus");

        // ✅ 精简的系统提示词（每次think都会发送，需要尽量简短）
        String SYSTEM_PROMPT = """
                You are Manus, a task-oriented AI assistant. Complete tasks efficiently, then STOP.
                
                CRITICAL: doTerminate() is your ONLY way to end tasks. Call it when:
                - All task parts completed + output generated in requested format, OR
                - Unable to proceed further
                
                Output format: Match user's request (PDF→generatePDF, file→writeFile, text→writeFile)
                Efficiency: Search before scrape, read before write, collect all info before generating output.
                Download only resources needed for final output.
                
                Before doTerminate: Verify task understood + info collected + output generated + all parts done.
                """;

        this.setSystemPrompt(SYSTEM_PROMPT);

        // ✅ 精简的下一步提示词（每次think都会发送，需要尽量简短）
        String NEXT_STEP_PROMPT = """
                Decide next action:
                1. Review conversation history - what's done? what's missing? what format needed?
                2. Choose the SINGLE most important next task and appropriate tool
                3. If complete: generate output in requested format → call doTerminate()
                   If stuck: generate partial output → call doTerminate()
                   Otherwise: use appropriate tool
                4. Don't repeat calls, combine info before output, match user's format.
                """;

        this.setNextStepPrompt(NEXT_STEP_PROMPT);

        // 增加步数以应对复杂任务
        this.setMaxSteps(5);

        // 初始化 AI 对话客户端
        ChatClient.Builder builder = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(new MyLoggerAdvisor());

        // 注意：不在这里设置 defaultToolCallbacks，避免与 ToolCallAgent.think() 中的 toolCallbacks 重复注册
        // 工具会在每次 think() 调用时通过 toolCallbacks(availableTools) 注册

        ChatClient chatClient = builder.build();
        this.setChatClient(chatClient);
    }
}