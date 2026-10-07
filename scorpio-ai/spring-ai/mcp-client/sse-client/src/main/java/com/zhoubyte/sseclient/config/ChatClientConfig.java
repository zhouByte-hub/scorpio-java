package com.zhoubyte.sseclient.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean("ollamaChatClient")
    public ChatClient ollamaChatClient(ChatClient.Builder chatClientBuilder,
                                       ObjectProvider<ToolCallbackProvider> toolCallbackProviders) {
        return chatClientBuilder
                .defaultTools(toolCallbackProviders.orderedStream().toArray())
                .build();
    }
}
