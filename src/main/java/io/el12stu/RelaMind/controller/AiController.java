package io.el12stu.RelaMind.controller;

import io.el12stu.RelaMind.agent.Manus;
import io.el12stu.RelaMind.app.RelaMindApp;
import jakarta.annotation.Resource;
import jakarta.inject.Provider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

/**
 * AI 聊天控制器
 * 
 * 产品接口设计：
 * 1. /ai/chat - 主聊天接口（智能路由，推荐使用）
 * 2. /ai/manus - 高级智能体接口（复杂任务）
 * 
 * 内部接口（用于调试/测试，不建议前端直接使用）：
 * - /ai/internal/* - 底层接口
 */
@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private RelaMindApp relaMindApp;

    // Controller 持有的是工厂，而不是Bean本身
    @Autowired
    private Provider<Manus> manusProvider;

    // ==================== 产品接口 ====================

    /**
     * 主聊天接口 - 智能路由（推荐使用）
     * 
     * 功能：
     * - 自动识别用户意图（普通聊天 / 历史查询 / 工具调用）
     * - 自动选择最合适的处理方式
     * - 支持工具调用（搜索、下载、生成文件等）
     * - 支持历史记录检索（RAG）
     * 
     * 使用场景：
     * - 日常聊天、情感陪伴
     * - 查询历史记录："我去年这个时候在做什么？"
     * - 需要工具调用："帮我搜索一下今天的天气"
     * 
     * @param message 用户消息
     * @param chatId 会话ID（用于多轮对话记忆）
     * @return SSE 流式响应
     */
    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(String message, String chatId) {
        return relaMindApp.smartChatByStream(message, chatId);
    }

    /**
     * 主聊天接口 - SseEmitter 版本（推荐前端使用）
     * 
     * 与 /chat 功能相同，但返回 SseEmitter，更适合前端 EventSource
     */
    @GetMapping("/chat/stream")
    public SseEmitter chatStream(String message, String chatId) {
        SseEmitter sseEmitter = new SseEmitter(180000L); // 3 分钟超时
        relaMindApp.smartChatByStream(message, chatId)
                .subscribe(chunk -> {
                    try {
                        sseEmitter.send(chunk);
                    } catch (IOException e) {
                        sseEmitter.completeWithError(e);
                    }
                }, sseEmitter::completeWithError, sseEmitter::complete);
        return sseEmitter;
    }

    /**
     * 主聊天接口 - 同步版本（用于测试或简单场景）
     */
    @GetMapping("/chat/sync")
    public String chatSync(String message, String chatId) {
        return relaMindApp.smartChat(message, chatId);
    }

    /**
     * 高级智能体接口 - Manus
     * 
     * 功能：
     * - 自主规划复杂任务
     * - 多步骤推理和执行
     * - 适合需要多工具协作的复杂场景
     * 
     * 使用场景：
     * - 制定完整的计划（如"帮我制定一个周末约会计划"）
     * - 需要多步骤操作的任务
     * 
     * @param message 用户消息
     * @return SSE 流式响应
     */
    @GetMapping("/manus")
    public SseEmitter manus(String message) {
        Manus manus = manusProvider.get();
        return manus.runStream(message);
    }

    // ==================== 内部接口（用于调试/测试）====================

    /**
     * 内部接口 - 普通聊天（不使用智能路由）
     * 仅用于调试，不建议前端使用
     */
    @GetMapping("/internal/chat/simple")
    public String internalSimpleChat(String message, String chatId) {
        return relaMindApp.doChat(message, chatId);
    }

    /**
     * 内部接口 - RAG 查询（强制使用 RAG）
     * 仅用于调试，不建议前端使用
     */
    @GetMapping("/internal/chat/rag")
    public String internalRagChat(String message, String chatId) {
        return relaMindApp.doChatWithRag(message, chatId);
    }

    /**
     * 内部接口 - 工具调用（强制使用工具）
     * 仅用于调试，不建议前端使用
     */
    @GetMapping("/internal/chat/tools")
    public String internalToolsChat(String message, String chatId) {
        return relaMindApp.doChatWithTools(message, chatId);
    }

    // ==================== 兼容旧接口（逐步废弃）====================
    
    /**
     * @deprecated 请使用 /ai/chat 替代
     */
    @Deprecated
    @GetMapping(value = "/RelaMind_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithRelaMindAppSSE(String message, String chatId) {
        return relaMindApp.doChatByStream(message, chatId);
    }

    /**
     * @deprecated 请使用 /ai/manus 替代
     */
    @Deprecated
    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        Manus manus = manusProvider.get();
        return manus.runStream(message);
    }
}


