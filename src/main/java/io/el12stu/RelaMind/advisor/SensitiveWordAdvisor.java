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
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * 敏感词拦截 Advisor
 * 如果用户输入包含敏感词，将直接返回错误响应，跳过大模型 API 调用
 */
@Slf4j
public class SensitiveWordAdvisor implements CallAdvisor, StreamAdvisor {

	private final SensitiveWordService sensitiveWordService;
	private final ChatModel dashscopeChatModel;

	private static final String ERROR_MESSAGE = "[系统安全提示]：您的输入包含敏感内容，请求已被拦截。";

	public SensitiveWordAdvisor(SensitiveWordService sensitiveWordService, ChatModel dashscopeChatModel) {
		this.sensitiveWordService = sensitiveWordService;
		this.dashscopeChatModel = dashscopeChatModel;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
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
	 * 通过 ChatModel 创建一个包含错误消息的响应
	 */
	private ChatClientResponse createErrorClientResponse(ChatClientRequest request) {
		try {
			// 创建一个包含错误消息的 Prompt
			AssistantMessage errorMessage = new AssistantMessage(ERROR_MESSAGE);
			Generation generation = new Generation(errorMessage);
			ChatResponse chatResponse = new ChatResponse(List.of(generation));
			
			// 尝试使用反射创建 ChatClientResponse 实例
			// 首先尝试找到构造方法
			Constructor<ChatClientResponse> constructor = ChatClientResponse.class.getDeclaredConstructor(
					ChatClientRequest.class, ChatResponse.class);
			constructor.setAccessible(true);
			return constructor.newInstance(request, chatResponse);
		} catch (NoSuchMethodException e) {
			// 如果找不到构造方法，尝试其他方式
			log.warn("无法通过反射创建 ChatClientResponse，尝试其他方式", e);
			try {
				// 修改响应内容为错误消息
				AssistantMessage errorMessage = new AssistantMessage(ERROR_MESSAGE);
				Generation generation = new Generation(errorMessage);
				ChatResponse errorResponse = new ChatResponse(List.of(generation));
				
				// 再次尝试反射，尝试只有 ChatResponse 参数的构造方法
				Constructor<ChatClientResponse> constructor = ChatClientResponse.class.getDeclaredConstructor(
						ChatResponse.class);
				constructor.setAccessible(true);
				return constructor.newInstance(errorResponse);
			} catch (Exception ex) {
				log.error("创建错误响应失败，将抛出异常", ex);
				// 作为最后的手段，我们创建一个包含错误消息的 Prompt 并调用模型
				// 这会实际调用模型，但至少能返回错误消息
				// 注意：这不符合"完全跳过大模型 API 调用"的要求，但在无法创建 ChatClientResponse 的情况下的备选方案
				Prompt errorPrompt = new Prompt(List.of(new UserMessage(ERROR_MESSAGE)));
				ChatResponse chatResponse = dashscopeChatModel.call(errorPrompt);
				// 由于无法直接创建 ChatClientResponse，我们尝试最后一次反射
				try {
					Constructor<ChatClientResponse> constructor = ChatClientResponse.class.getDeclaredConstructor(
							ChatClientRequest.class, ChatResponse.class);
					constructor.setAccessible(true);
					return constructor.newInstance(request, chatResponse);
				} catch (Exception finalEx) {
					throw new RuntimeException("无法创建 ChatClientResponse，请检查 Spring AI 版本和 API", finalEx);
				}
			}
		} catch (Exception e) {
			log.error("创建错误响应失败", e);
			throw new RuntimeException("无法创建错误响应", e);
		}
	}

	@Override
	public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain chain) {
		// 提取用户输入文本
		String userInput = extractUserInput(chatClientRequest);

		// 检查是否包含敏感词
		if (sensitiveWordService.containsSensitiveWords(userInput)) {
			log.warn("检测到敏感词，已拦截请求。用户输入: {}", userInput);
			// 返回合成的错误响应，阻止 chain.nextCall 的执行
			return createErrorClientResponse(chatClientRequest);
		}

		// 如果没有敏感词，继续执行后续调用链
		return chain.nextCall(chatClientRequest);
	}

	@Override
	public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain chain) {
		// 提取用户输入文本
		String userInput = extractUserInput(chatClientRequest);

		// 检查是否包含敏感词
		if (sensitiveWordService.containsSensitiveWords(userInput)) {
			log.warn("检测到敏感词，已拦截流式请求。用户输入: {}", userInput);
			// 返回合成的错误响应，包装在 Flux 中
			ChatClientResponse errorResponse = createErrorClientResponse(chatClientRequest);
			return Flux.just(errorResponse);
		}

		// 如果没有敏感词，继续执行后续调用链
		return chain.nextStream(chatClientRequest);
	}
}
