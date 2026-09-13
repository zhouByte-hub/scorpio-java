package com.zhoubyte.core.config;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.ToolCallingAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.advisor.observation.AdvisorObservationConvention;
import org.springframework.ai.chat.client.observation.ChatClientObservationConvention;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.chat.client.autoconfigure.ChatClientBuilderConfigurer;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.LinkedList;
import java.util.List;

/**
 * ChatClient 的两种组装方式演示。
 * <p>
 * 容器里会有两个 {@link ChatClient}：{@code ollamaChatClient}（默认）和
 * {@code ollamaConfiguredChatClient}。直接注入 {@code ChatClient} 走前者；
 * 要用后者时加 {@code @Qualifier("ollamaConfiguredChatClient")}。
 *
 * @author zhoubyte
 */
@Configuration
public class ChatClientConfig {

    /**
     * 单模型：注入自动配置的 {@link ChatClient.Builder}。
     * Builder 是 prototype，每个注入点一份新实例，可分别设 defaultOptions / defaultSystem。
     * {@code @Primary} 避免出现两个 ChatClient 时无法唯一注入。
     */
    @Bean
    @Primary
    public ChatClient ollamaChatClient(ChatClient.Builder chatClientBuilder, List<BaseAdvisor> advisors,
                                       ChatMemory messageMysqlChatMemory) {
        MessageChatMemoryAdvisor memoryAdvisor = MessageChatMemoryAdvisor.builder(messageMysqlChatMemory).order(0).build();
        advisors.add(memoryAdvisor);

        return chatClientBuilder
                .defaultAdvisors(memoryAdvisor)
                .defaultOptions(defaultOllamaOptions())
                .build();
    }

    /**
     * 多模型：按具体 ChatModel 类型组装，不能再用自动配置的 Builder
     * （多个 ChatModel 时它无法唯一解析）。
     * 必须经 {@link ChatClientBuilderConfigurer}，否则会丢掉 Observation 和 Customizer。
     * 当前只有 Ollama；接入 OpenAI 时再写一个 Bean，注入 {@code OpenAiChatModel} 即可。
     */
//    @Bean
    public ChatClient ollamaConfiguredChatClient(
            OllamaChatModel chatModel,
            ChatClientBuilderConfigurer configurer,
            ObjectProvider<ObservationRegistry> observationRegistry,
            ObjectProvider<ChatClientObservationConvention> chatClientObservationConvention,
            ObjectProvider<AdvisorObservationConvention> advisorObservationConvention,
            ObjectProvider<ToolCallingAdvisor.Builder<?>> toolCallingAdvisorBuilder) {
        return buildChatClient(
                chatModel,
                configurer,
                observationRegistry,
                chatClientObservationConvention,
                advisorObservationConvention,
                toolCallingAdvisorBuilder)
                .defaultOptions(defaultOllamaOptions())
                .build();
    }

    /**
     * 与自动配置等价的 Builder 创建过程，供多个模型 Bean 复用。
     */
    private ChatClient.Builder buildChatClient(
            ChatModel chatModel,
            ChatClientBuilderConfigurer configurer,
            ObjectProvider<ObservationRegistry> observationRegistry,
            ObjectProvider<ChatClientObservationConvention> chatClientObservationConvention,
            ObjectProvider<AdvisorObservationConvention> advisorObservationConvention,
            ObjectProvider<ToolCallingAdvisor.Builder<?>> toolCallingAdvisorBuilder) {
        ChatClient.Builder builder = ChatClient.builder(
                chatModel,
                // 没有 ObservationRegistry 时用 NOOP，避免 NPE，也保证后续可接 Micrometer
                observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP),
                chatClientObservationConvention.getIfUnique(),
                advisorObservationConvention.getIfUnique(),
                toolCallingAdvisorBuilder.getIfAvailable());
        return configurer.configure(builder);
    }

    /**
     * Spring AI 2.0 的 {@code defaultOptions} 要传 Builder，不能传已经 {@code build()} 的 ChatOptions，
     * 这样才会和模型默认值做字段合并。Ollama 专用选项用 {@link OllamaChatOptions}，不要用可移植的 ChatOptions。
     */
    private OllamaChatOptions.Builder defaultOllamaOptions() {
        return OllamaChatOptions.builder()
                .topK(5)
                .temperature(1.0);
    }
}
