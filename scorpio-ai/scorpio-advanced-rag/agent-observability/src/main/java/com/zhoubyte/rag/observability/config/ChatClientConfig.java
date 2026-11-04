package com.zhoubyte.rag.observability.config;

import com.zhoubyte.rag.observability.tool.OrderTools;
import com.zhoubyte.rag.observability.tool.TimeTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Agent 装配。
 *
 * <p>无需手写任何埋点：Spring AI 自动配置向 ObservationRegistry 注册
 * ChatClient / ChatModel / ToolCallback 的观测器，只要类路径上存在
 * micrometer-tracing-bridge-otel + opentelemetry-exporter-otlp，
 * 每次“对话-工具-对话”多跳都会产出完整的 OpenTelemetry Span 树。</p>
 */
@Configuration
public class ChatClientConfig {

    /**
     * 进程内滑动窗口会话记忆（观测示例无需外部存储）。
     */
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(20)
                .build();
    }

    /**
     * Agent 入口：挂载记忆切面与工具，一次提问可触发“模型→工具→模型”多级调用，
     * Langfuse 中呈现为多层 Span 树。
     */
    @Bean
    public ChatClient agentClient(ChatClient.Builder builder, ChatMemory chatMemory,
                                  TimeTools timeTools, OrderTools orderTools) {
        return builder
                .defaultSystem("你是一个订单助手，可以用工具查询当前时间和订单信息，回答请使用简体中文。")
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultTools(timeTools, orderTools)
                .build();
    }
}
