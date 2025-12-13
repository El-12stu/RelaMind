package io.el12stu.RelaMind.advisor;

import io.el12stu.RelaMind.service.SensitiveWordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.List;

/**
 * 敏感词拦截 Advisor
 * 如果用户输入包含敏感词，将直接返回错误响应，跳过大模型 API 调用
 */
@Slf4j
public class SensitiveWordAdvisor implements CallAdvisor, StreamAdvisor {

	private final SensitiveWordService sensitiveWordService;

	private static final String ERROR_MESSAGE = "[系统安全提示]：您的输入包含敏感内容，请求已被拦截。";

	public SensitiveWordAdvisor(SensitiveWordService sensitiveWordService, @SuppressWarnings("unused") org.springframework.ai.chat.model.ChatModel dashscopeChatModel) {
		this.sensitiveWordService = sensitiveWordService;
	}

	@Override
	@NonNull
	public String getName() {
		String name = this.getClass().getSimpleName();
		return name != null ? name : "SensitiveWordAdvisor";
	}

	@Override
	public int getOrder() {
		// 设置较高的优先级，确保在其他 Advisor 之前执行
		return -100;
	}

	/**
	 * 从 ChatClientRequest 中提取用户输入文本
	 * 获取最后一条 UserMessage 的内容作为检测目标
	 */
	private String extractUserInput(ChatClientRequest request) {
		try {
			Prompt prompt = request.prompt();
			List<org.springframework.ai.chat.messages.Message> messages = prompt.getInstructions();

			// 从后往前查找最后一条 UserMessage
			for (int i = messages.size() - 1; i >= 0; i--) {
				org.springframework.ai.chat.messages.Message message = messages.get(i);
				if (message instanceof UserMessage) {
					return ((UserMessage) message).getText();
				}
			}

			// 如果没有找到 UserMessage，尝试使用 prompt 的便捷方法
			try {
				return prompt.getUserMessage().getText();
			} catch (Exception e) {
				log.warn("无法提取用户输入文本", e);
				return "";
			}
		} catch (Exception e) {
			log.error("提取用户输入时发生错误", e);
			return "";
		}
	}

	/**
	 * 创建包含错误信息的 ChatClientResponse
	 * 采用直接构造方法，实现零 API 调用拦截。
	 * 依赖 Spring AI 库中 ChatClientResponse 的 Record 公共构造函数签名:
	 * public ChatClientResponse(@Nullable ChatResponse chatResponse, Map<String, Object> context)
	 */
	@NonNull
	private ChatClientResponse createErrorClientResponse(@NonNull ChatClientRequest request) {
		try {
			// 1. 构造包含错误消息的 ChatResponse 对象
			AssistantMessage errorMessage = new AssistantMessage(ERROR_MESSAGE);
			Generation generation = new Generation(errorMessage);
			ChatResponse chatResponse = new ChatResponse(List.of(generation));
			
			// 2. 使用当前 Spring AI 版本最稳定的公共构造函数直接实例化，
			//    实现对大模型的零 API 调用。
			//    我们提供 chatResponse 和一个空的 context Map。
			return new ChatClientResponse(chatResponse, Collections.emptyMap());
			
		} catch (Exception e) {
			// 如果这里仍然失败，说明 ChatClientResponse 的公共构造函数签名仍然不同。
			log.error("无法通过公共构造函数创建 ChatClientResponse，请检查 Spring AI 依赖版本。", e);
			throw new RuntimeException("无法创建 ChatClientResponse，请检查 Spring AI 版本和 API", e);
		}
	}

	@Override
	@NonNull
	public ChatClientResponse adviseCall(@NonNull ChatClientRequest chatClientRequest, @NonNull CallAdvisorChain chain) {
		// 提取用户输入文本
		String userInput = extractUserInput(chatClientRequest);

		// 检查是否包含敏感词
		if (sensitiveWordService.containsSensitiveWords(userInput)) {
			log.warn("检测到敏感词，已拦截请求。用户输入: {}", userInput);
			// 直接返回错误响应，实现零 API 调用拦截
			return createErrorClientResponse(chatClientRequest);
		}

		// 如果没有敏感词，继续执行后续调用链
		return chain.nextCall(chatClientRequest);
	}

	@Override
	@NonNull
	public Flux<ChatClientResponse> adviseStream(@NonNull ChatClientRequest chatClientRequest, @NonNull StreamAdvisorChain chain) {
		// 提取用户输入文本
		String userInput = extractUserInput(chatClientRequest);

		// 检查是否包含敏感词
		if (sensitiveWordService.containsSensitiveWords(userInput)) {
			log.warn("检测到敏感词，已拦截流式请求。用户输入: {}", userInput);
			// 直接返回错误响应，实现零 API 调用拦截
			ChatClientResponse errorResponse = createErrorClientResponse(chatClientRequest);
			return Flux.just(errorResponse);
		}

		// 如果没有敏感词，继续执行后续调用链
		return chain.nextStream(chatClientRequest);
	}
}
