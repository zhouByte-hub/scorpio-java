package com.zhoubyte.rag.ingestion.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 图片内容描述使用的视觉模型 ChatClient。
 *
 * <p>默认对话模型（qwen2.5）不具备视觉能力，因此为图片解析单独装配一个
 * 以视觉模型（qwen2.5vl 等）为默认 model 的 ChatClient，低温保证描述稳定。</p>
 */
@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient visionClient(ChatClient.Builder builder, RagIngestionProperties properties) {
        return builder
                .defaultOptions(OllamaChatOptions.builder()
                        .model(properties.vlmModel())
                        .temperature(0.15)
                        .build())
                .build();
    }
}
