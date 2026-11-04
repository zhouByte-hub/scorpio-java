package com.zhoubyte.rag.pipeline.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 流水线各环节使用的 ChatClient 装配。
 *
 * <p>不同环节对模型“温度”的要求不同：
 * <ul>
 *   <li>{@code utilityClient}：意图分类 / Query 改写 / 稀疏词项扩展 / 重排打分，要求稳定，低温度；</li>
 *   <li>{@code generationClient}：基于检索上下文生成答案，允许轻微表达自由度；</li>
 *   <li>{@code verifyClient}：事实校验，必须极度保守，温度接近 0。</li>
 * </ul>
 * </p>
 */
@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient utilityClient(ChatClient.Builder builder) {
        return builder
                .defaultOptions(OllamaChatOptions.builder().temperature(0.2).build())
                .build();
    }

    @Bean
    public ChatClient generationClient(ChatClient.Builder builder) {
        return builder
                .defaultOptions(OllamaChatOptions.builder().temperature(0.4).build())
                .build();
    }

    @Bean
    public ChatClient verifyClient(ChatClient.Builder builder) {
        return builder
                .defaultOptions(OllamaChatOptions.builder().temperature(0.05).build())
                .build();
    }
}
