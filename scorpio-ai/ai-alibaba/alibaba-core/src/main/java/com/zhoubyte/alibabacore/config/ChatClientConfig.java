package com.zhoubyte.alibabacore.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhoubyte.alibabacore.memory.RedisChatMemoryRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

/**
 * ChatClient 全局装配。
 *
 * <p>ChatClient 是 Spring AI 面向应用层的统一对话入口，采用 Builder 模式创建。
 * {@code ChatClient.Builder} 由 spring-ai-starter-model-ollama 自动装配，
 * 内部持有自动配置的 OllamaChatModel。</p>
 *
 * <p>会话记忆通过 Advisor（切面）机制实现：
 * <ul>
 *   <li>{@link MessageWindowChatMemory}：滑动窗口记忆，只保留最近 N 条消息，防止上下文无限膨胀；</li>
 *   <li>{@link MessageChatMemoryAdvisor}：在每次请求前把历史消息注入 Prompt，请求后把新消息写回记忆。</li>
 * </ul>
 * </p>
 */
@Configuration
public class ChatClientConfig {

    /**
     * 会话记忆存储后端：Redis 持久化实现（spring-ai 1.1.2 无官方 Redis Repository，自研）。
     * spring.data.redis.* 配置见 application.yaml。
     */
    @Bean
    public ChatMemoryRepository chatMemoryRepository(StringRedisTemplate stringRedisTemplate,
                                                     ObjectMapper objectMapper) {
        return new RedisChatMemoryRepository(stringRedisTemplate, objectMapper, Duration.ofDays(7));
    }

    /**
     * 滑动窗口会话记忆：保留最近 10 条消息，历史持久化到 Redis，应用重启后记忆不丢失。
     */
    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(10)
                .build();
    }

    /**
     * 全局 ChatClient：
     * - defaultSystem：所有请求共用的系统提示词；
     * - defaultAdvisors：挂载记忆切面，之后每次调用只需传入 conversationId 即可多轮对话。
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory) {
        return builder
                .defaultSystem("你是一个乐于助人的 AI 助手，请使用简体中文回答问题。")
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
}
