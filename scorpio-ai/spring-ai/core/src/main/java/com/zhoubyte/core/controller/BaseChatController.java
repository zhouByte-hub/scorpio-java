package com.zhoubyte.core.controller;

import com.zhoubyte.core.pojo.dto.ChatMessageRequest;
import com.zhoubyte.core.pojo.dto.ChatMessageResponse;
import com.zhoubyte.core.service.ChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;


@RestController
@RequestMapping("/chat")
public class BaseChatController {

    private final ChatService chatService;

    public BaseChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping(value = "/message", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ChatMessageResponse chat(@RequestBody ChatMessageRequest request) {
        return new ChatMessageResponse(chatService.chat(request == null ? null : request.message()));
    }

    @PostMapping(
            value = "/stream",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestBody ChatMessageRequest request) {
        return chatService.stream(request == null ? null : request.message());
    }
}
