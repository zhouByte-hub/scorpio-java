package com.zhoubyte.stdioclient.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean("ollamaChatClient")
    public ChatClient ollamaChatClient(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder.build();
    }
}
