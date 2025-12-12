package io.el12stu.RelaMind.controller;

import io.el12stu.RelaMind.agent.Manus;
import io.el12stu.RelaMind.app.RelaMindApp;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private RelaMindApp relaMindApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;

    /**
     * 同步调用RelaMind应用
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping("/RelaMind_app/chat/sync")
    public String doChatWithRelaMindAppSync(String message, String chatId) {
        return relaMindApp.doChat(message, chatId);
    }

    /**
     * SSE 流式调用RelaMind应用
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/RelaMind_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLoveAppSSE(String message, String chatId) {
        return relaMindApp.doChatByStream(message, chatId);
    }

    /**
     * SSE 流式调用RelaMind应用
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/RelaMind_app/chat/server_sent_event")
    public Flux<ServerSentEvent<String>> doChatWithRelaMindAppServerSentEvent(String message, String chatId) {
        return relaMindApp.doChatByStream(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    /**
     * SSE 流式调用RelaMind应用
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/RelaMind_app/chat/sse_emitter")
    public SseEmitter doChatWithRelaMindAppServerSseEmitter(String message, String chatId) {
        // 创建一个超时时间较长的 SseEmitter
        SseEmitter sseEmitter = new SseEmitter(180000L); // 3 分钟超时
        // 获取 Flux 响应式数据流并且直接通过订阅推送给 SseEmitter
        relaMindApp.doChatByStream(message, chatId)
                .subscribe(chunk -> {
                    try {
                        sseEmitter.send(chunk);
                    } catch (IOException e) {
                        sseEmitter.completeWithError(e);
                    }
                }, sseEmitter::completeWithError, sseEmitter::complete);
        // 返回
        return sseEmitter;
    }

    /**
     * 流式调用 Manus 超级智能体
     *
     * @param message
     * @return
     */
    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        Manus manus = new Manus(allTools, dashscopeChatModel);
        return manus.runStream(message);
    }
}
