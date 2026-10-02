package com.zhoubyte.core.service;

import reactor.core.publisher.Flux;

public interface ChatService {

    String chat(String message);

    Flux<String> stream(String message, String conversationId);
}
