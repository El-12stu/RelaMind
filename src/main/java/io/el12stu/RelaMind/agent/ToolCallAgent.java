package io.el12stu.RelaMind.agent;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import io.el12stu.RelaMind.agent.model.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 处理工具调用的基础代理类，具体实现了 think 和 act 方法，可以用作创建实例的父类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReActAgent {

    // 可用的工具
    private final ToolCallback[] availableTools;

    // 保存工具调用信息的响应结果（要调用那些工具）
    private ChatResponse toolCallChatResponse;
    
    // 保存当前步骤的思考内容
    private String currentThought = "";

    // 工具调用管理者
    private final ToolCallingManager toolCallingManager;

    // 禁用 Spring AI 内置的工具调用机制，自己维护选项和消息上下文
    private final ChatOptions chatOptions;

    public ToolCallAgent(ToolCallback[] availableTools) {
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        // 禁用 Spring AI 内置的工具调用机制，自己维护选项和消息上下文
        this.chatOptions = DashScopeChatOptions.builder()
                .withInternalToolExecutionEnabled(false)
                .build();
    }

    /**
     * 处理当前状态并决定下一步行动
     *
     * @return 是否需要执行行动
     */
    @Override
    public boolean think() {
        // 1、校验提示词，拼接用户提示词（动态添加当前步数信息）
        if (StrUtil.isNotBlank(getNextStepPrompt())) {
            // 构建包含当前步数信息的提示词
            String stepInfoPrompt = buildStepInfoPrompt();
            UserMessage userMessage = new UserMessage(stepInfoPrompt);
            getMessageList().add(userMessage);
        }
        // 2、调用 AI 大模型，获取工具调用结果
        List<Message> messageList = getMessageList();
        Prompt prompt = new Prompt(messageList, this.chatOptions);
        try {
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .toolCallbacks(availableTools)
                    .call()
                    .chatResponse();
            // 记录响应，用于等下 Act
            this.toolCallChatResponse = chatResponse;
            // 3、解析工具调用结果，获取要调用的工具
            // 助手消息
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            // 获取要调用的工具列表
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
            // 输出提示信息
            String result = assistantMessage.getText();
            // 保存思考内容，供 step() 方法使用
            this.currentThought = result != null ? result : "";
            log.info(getName() + "的思考：" + this.currentThought);
            log.info(getName() + "选择了 " + toolCallList.size() + " 个工具来使用");
            String toolCallInfo = toolCallList.stream()
                    .map(toolCall -> String.format("工具名称：%s，参数：%s", toolCall.name(), toolCall.arguments()))
                    .collect(Collectors.joining("\n"));
            log.info(toolCallInfo);
            
            // 如果没有工具调用，返回格式化的思考结果
            if (toolCallList.isEmpty()) {
                // 只有不调用工具时，才需要手动记录助手消息
                getMessageList().add(assistantMessage);
                return false;
            }
            // 需要调用工具时，无需记录助手消息，因为调用工具时会自动记录
            return true;
        } catch (Exception e) {
            log.error(getName() + "的思考过程遇到了问题：" + e.getMessage());
            getMessageList().add(new AssistantMessage("处理时遇到了错误：" + e.getMessage()));
            return false;
        }
    }

    /**
     * 执行工具调用并处理结果
     *
     * @return 执行结果（格式化的 JSON 字符串，包含工具名称列表和简要结果）
     */
    @Override
    public String act() {
        if (!toolCallChatResponse.hasToolCalls()) {
            return "{\"action\":\"无需执行行动\"}";
        }
        // 调用工具
        Prompt prompt = new Prompt(getMessageList(), this.chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
        // 记录消息上下文，conversationHistory 已经包含了助手消息和工具调用返回的结果
        setMessageList(toolExecutionResult.conversationHistory());
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        // 判断是否调用了终止工具
        boolean terminateToolCalled = toolResponseMessage.getResponses().stream()
                .anyMatch(response -> response.name().equals("doTerminate"));
        if (terminateToolCalled) {
            // 任务结束，更改状态
            setState(AgentState.FINISHED);
            
            // 尝试从消息历史中提取最终答案（最后一次助手消息的文本）
            String finalAnswer = "";
            if (getMessageList() != null && !getMessageList().isEmpty()) {
                for (int i = getMessageList().size() - 1; i >= 0; i--) {
                    org.springframework.ai.chat.messages.Message msg = getMessageList().get(i);
                    if (msg instanceof org.springframework.ai.chat.messages.AssistantMessage) {
                        String text = ((org.springframework.ai.chat.messages.AssistantMessage) msg).getText();
                        if (text != null && !text.trim().isEmpty() && !text.contains("doTerminate")) {
                            finalAnswer = text;
                            break;
                        }
                    }
                }
            }
            
            // 提取工具名称列表
            List<String> toolNames = toolResponseMessage.getResponses().stream()
                    .map(response -> response.name())
                    .collect(Collectors.toList());
            
            // 构建包含最终答案的 JSON
            String toolNamesStr = String.join(", ", toolNames);
            String result;
            if (!finalAnswer.isEmpty()) {
                result = String.format("{\"action\":\"任务完成\",\"tools\":[%s],\"answer\":\"%s\"}", 
                        toolNames.stream()
                                .map(name -> "\"" + name + "\"")
                                .collect(Collectors.joining(",")),
                        finalAnswer.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n"));
            } else {
                result = String.format("{\"action\":\"任务完成\",\"tools\":[%s]}", 
                        toolNames.stream()
                                .map(name -> "\"" + name + "\"")
                                .collect(Collectors.joining(",")));
            }
            
            log.info("任务完成，调用的工具: {}", toolNamesStr);
            return result;
        }
        
        // 提取工具名称列表（不包含详细结果，避免前端显示过多技术细节）
        List<String> toolNames = toolResponseMessage.getResponses().stream()
                .map(response -> response.name())
                .collect(Collectors.toList());
        
        // 检测文件生成工具（writeFile, generatePDF），提取文件信息
        List<String> fileInfos = new ArrayList<>();
        for (var response : toolResponseMessage.getResponses()) {
            String toolName = response.name();
            String responseData = response.responseData();
            
            // 检查是否是文件生成工具
            if (("writeFile".equals(toolName) || "generatePDF".equals(toolName)) && responseData != null) {
                // 尝试解析 JSON 格式的响应
                try {
                    // 检查是否是 JSON 格式
                    if (responseData.trim().startsWith("{")) {
                        // 提取文件信息（简化处理，直接使用响应数据）
                        fileInfos.add(responseData);
                    }
                } catch (Exception e) {
                    log.warn("解析文件信息失败: {}", e.getMessage());
                }
            }
        }
        
        // 构建简化的结果 JSON（只包含工具名称，不包含详细响应数据）
        String toolNamesStr = String.join(", ", toolNames);
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append("{\"action\":\"调用工具\",\"tools\":[");
        resultBuilder.append(toolNames.stream()
                .map(name -> "\"" + name + "\"")
                .collect(Collectors.joining(",")));
        resultBuilder.append("]");
        
        // 如果有文件信息，添加到结果中
        if (!fileInfos.isEmpty()) {
            resultBuilder.append(",\"files\":[");
            resultBuilder.append(String.join(",", fileInfos));
            resultBuilder.append("]");
        }
        
        resultBuilder.append("}");
        String result = resultBuilder.toString();
        
        // 详细结果只记录到日志，不返回给前端
        String detailedResults = toolResponseMessage.getResponses().stream()
                .map(response -> "工具 " + response.name() + " 返回的结果：" + response.responseData())
                .collect(Collectors.joining("\n"));
        log.info("调用的工具: {}", toolNamesStr);
        log.info("详细结果（仅日志）: {}", detailedResults);
        
        return result;
    }

    /**
     * 构建包含当前步数信息的提示词
     *
     * @return 包含步数信息的提示词
     */
    private String buildStepInfoPrompt() {
        String basePrompt = getNextStepPrompt();
        int currentStep = getCurrentStep();
        int maxSteps = getMaxSteps();
        
        // 在提示词开头添加步数信息
        String stepInfo = String.format("""
                ============================================
                CURRENT STEP: %d / %d
                ============================================
                
                """, currentStep, maxSteps);
        
        // 如果接近或超过最大步数，添加警告信息
        if (currentStep >= maxSteps * 0.8) {
            int remainingSteps = maxSteps - currentStep;
            stepInfo += String.format("""
                    ⚠️ WARNING: You are on step %d of %d. Only %d steps remaining!
                    You should prepare to finalize the task and call doTerminate soon.
                    
                    """, currentStep, maxSteps, remainingSteps);
        }
        
        return stepInfo + basePrompt;
    }
}
