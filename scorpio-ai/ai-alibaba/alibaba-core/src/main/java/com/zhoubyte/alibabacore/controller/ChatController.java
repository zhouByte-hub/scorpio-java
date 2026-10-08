package com.zhoubyte.alibabacore.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * ChatClient 基础能力示例：同步调用、流式调用、多轮会话记忆。
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    public ChatController(ChatClient chatClient, ChatMemory chatMemory) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
    }

    /**
     * 同步单轮对话：call() 阻塞等待模型完整生成后一次性返回。
     * 示例：/chat/simple?message=介绍一下杭州
     */
    @GetMapping("/simple")
    public String simple(@RequestParam String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    /**
     * 流式对话：stream() 以 SSE（text/event-stream）逐 token 推送，首字延迟更低。
     * 示例：curl -N "http://localhost:8090/alibaba-core/chat/stream?message=写一首关于春天的短诗"
     */
    @GetMapping(value = "/stream", produces = "text/event-stream;charset=UTF-8")
    public Flux<String> stream(@RequestParam String message) {
        return chatClient.prompt()
                .user(message)
                .stream()
                .content();
    }

    /**
     * 多轮对话：通过 ChatMemory.CONVERSATION_ID 参数区分会话。
     * 相同 conversationId 的请求会携带历史消息，实现上下文连续。
     * 示例：
     *   /chat/memory?message=我叫小明&conversationId=user-1
     *   /chat/memory?message=我叫什么名字&conversationId=user-1   → 能答出“小明”
     */
    @GetMapping("/memory")
    public String memory(@RequestParam String message,
                         @RequestParam(defaultValue = "default") String conversationId) {
        return chatClient.prompt()
                // Advisor 参数：告诉记忆切面当前会话 ID
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(message)
                .call()
                .content();
    }

    /**
     * 清空指定会话的历史记忆。
     * 示例：/chat/memory/clear?conversationId=user-1
     */
    @DeleteMapping("/memory")
    public String clearMemory(@RequestParam(defaultValue = "default") String conversationId) {
        chatMemory.clear(conversationId);
        return "会话 [" + conversationId + "] 的记忆已清空";
    }
}
