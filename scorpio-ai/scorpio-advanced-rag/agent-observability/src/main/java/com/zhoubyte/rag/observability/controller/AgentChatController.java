package com.zhoubyte.rag.observability.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Agent 对话入口：conversationId 作为会话维度，Langfuse 中可按 session 聚合查看多轮 Trace。
 */
@RestController
@RequestMapping("/chat")
public class AgentChatController {

    private final ChatClient agentClient;

    public AgentChatController(ChatClient agentClient) {
        this.agentClient = agentClient;
    }

    public record ChatRequest(String conversationId, String message) {
    }

    public record ChatResponse(String conversationId, String answer) {
    }

    /**
     * 提问示例：
     * <pre>curl -X POST http://localhost:8110/agent-observability/chat -H 'Content-Type: application/json' \
     *   -d '{"conversationId":"c-001","message":"现在几点了？帮我查一下订单 1001 的状态"}'</pre>
     */
    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String answer = agentClient.prompt()
                .user(request.message())
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, request.conversationId()))
                .call()
                .content();
        return new ChatResponse(request.conversationId(), answer);
    }
}
