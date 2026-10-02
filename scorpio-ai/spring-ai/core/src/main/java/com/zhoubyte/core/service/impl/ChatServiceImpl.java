package com.zhoubyte.core.service.impl;

import com.zhoubyte.core.service.ChatService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

/**
 * 用自动配置的 {@link ChatClient.Builder} 组装可移植的 ChatClient，
 * 不直接依赖 {@code OllamaChatModel}，后续换模型不用改调用方。
 */
@Service
public class ChatServiceImpl implements ChatService {

    @Resource
    private ChatClient chatClient;

    @Override
    public String chat(String message) {
        String content = chatClient.prompt()
                .user(requireMessage(message))
                .call()
                .content();
        return content == null ? "" : content;
    }

    @Override
    public Flux<String> stream(String message, String conversationId) {
        return chatClient.prompt()
                .user(requireMessage(message))
                .advisors(spec -> spec.param("chat_memory_conversation_id", conversationId))
                .stream()
                .content();
    }

    private String requireMessage(String message) {
        if (message == null || message.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "message 不能为空");
        }
        return message;
    }
}
