package com.zhoubyte.alibabamcpclient.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ChatClient 装配：把 MCP Client 自动发现的远程 Tool 注册为默认工具。
 *
 * <p>spring-ai-starter-mcp-client 会把 alibaba-mcp-server 暴露的
 * query_order / query_logistics 等工具封装成 ToolCallbackProvider Bean，
 * 这里通过 defaultTools 绑定给 ChatClient，模型即可按需远程调用。</p>
 */
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
