package io.el12stu.RelaMind.agent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * ReAct (Reasoning and Acting) 模式的代理抽象类
 * 实现了思考-行动的循环模式
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public abstract class ReActAgent extends BaseAgent {

    /**
     * 处理当前状态并决定下一步行动
     *
     * @return 是否需要执行行动，true表示需要执行，false表示不需要执行
     */
    public abstract boolean think();

    /**
     * 执行决定的行动
     *
     * @return 行动执行结果
     */
    public abstract String act();

    /**
     * 执行单个步骤：思考和行动
     *
     * @return 步骤执行结果（JSON 格式，包含思考内容和行动信息）
     */
    @Override
    public String step() {
        try {
            // 先思考
            boolean shouldAct = think();
            
            // 获取思考内容
            String thought = "";
            if (this instanceof ToolCallAgent) {
                // 如果是 ToolCallAgent，从 currentThought 中获取
                thought = ((ToolCallAgent) this).getCurrentThought();
            }
            
            // 如果还是空的，尝试从消息列表中查找
            if (thought == null || thought.isEmpty()) {
                if (getMessageList() != null && !getMessageList().isEmpty()) {
                    // 查找最后一条助手消息
                    for (int i = getMessageList().size() - 1; i >= 0; i--) {
                        org.springframework.ai.chat.messages.Message msg = getMessageList().get(i);
                        if (msg instanceof org.springframework.ai.chat.messages.AssistantMessage) {
                            thought = ((org.springframework.ai.chat.messages.AssistantMessage) msg).getText();
                            break;
                        }
                    }
                }
            }
            
            if (thought == null) {
                thought = "";
            }
            
            if (!shouldAct) {
                // 构建 JSON 格式的返回结果
                return String.format("{\"thought\":\"%s\",\"action\":\"无需执行行动\"}", 
                        escapeJson(thought.isEmpty() ? "思考完成" : thought));
            }
            
            // 再行动
            String actionResult = act();
            
            // 如果行动结果是 JSON，解析并合并；否则包装成 JSON
            if (actionResult.trim().startsWith("{")) {
                // 解析 actionResult JSON
                try {
                    // 提取 action 和 tools 字段
                    String actionPart = actionResult;
                    // 合并思考内容和行动结果
                    return String.format("{\"thought\":\"%s\",%s}", 
                            escapeJson(thought), 
                            actionPart.substring(1)); // 去掉开头的 {
                } catch (Exception e) {
                    // 如果解析失败，直接合并
                    return String.format("{\"thought\":\"%s\",\"action\":\"%s\"}", 
                            escapeJson(thought), 
                            escapeJson(actionResult));
                }
            } else {
                // 如果不是 JSON，包装成 JSON
                return String.format("{\"thought\":\"%s\",\"action\":\"%s\"}", 
                        escapeJson(thought), 
                        escapeJson(actionResult));
            }
        } catch (Exception e) {
            // 记录异常日志
            e.printStackTrace();
            return String.format("{\"thought\":\"\",\"action\":\"步骤执行失败\",\"error\":\"%s\"}", 
                    escapeJson(e.getMessage()));
        }
    }
    
    /**
     * 转义 JSON 字符串中的特殊字符
     */
    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

}
