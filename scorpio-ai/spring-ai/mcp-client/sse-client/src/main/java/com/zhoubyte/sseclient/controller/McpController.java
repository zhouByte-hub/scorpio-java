package com.zhoubyte.sseclient.controller;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/mcp")
public class McpController {

    private final ObjectProvider<List<McpSyncClient>> mcpSyncClients;

    private final ChatClient ollamaChatClient;

    public McpController(ObjectProvider<List<McpSyncClient>> mcpSyncClients,
                         @Qualifier("ollamaChatClient") ChatClient ollamaChatClient) {
        this.mcpSyncClients = mcpSyncClients;
        this.ollamaChatClient = ollamaChatClient;
    }

    /**
     * 读取 MCP Server 资源，例如：/mcp/resource?uri=doc://1001
     */
    @GetMapping(value = "/resource")
    public String readResource(@RequestParam("uri") String uri) {
        McpSchema.ReadResourceResult result = mcpClient()
                .readResource(McpSchema.ReadResourceRequest.builder(uri).build());
        return result.contents().stream()
                .filter(McpSchema.TextResourceContents.class::isInstance)
                .map(content -> ((McpSchema.TextResourceContents) content).text())
                .collect(Collectors.joining("\n"));
    }

    /**
     * 调用 MCP Server 的 Prompt 模板（expert-answer），并将模板内容交给模型回答。
     * 例如：/mcp/prompt?question=什么是MCP
     */
    @GetMapping(value = "/prompt")
    public String prompt(@RequestParam("question") String question) {
        McpSchema.GetPromptResult result = mcpClient()
                .getPrompt(McpSchema.GetPromptRequest.builder("expert-answer")
                        .arguments(Map.of("question", question))
                        .build());
        String promptText = result.messages().stream()
                .map(McpSchema.PromptMessage::content)
                .filter(McpSchema.TextContent.class::isInstance)
                .map(content -> ((McpSchema.TextContent) content).text())
                .collect(Collectors.joining("\n"));
        return ollamaChatClient.prompt(promptText).call().content();
    }

    private McpSyncClient mcpClient() {
        List<McpSyncClient> clients = mcpSyncClients.getObject();
        if (clients.isEmpty()) {
            throw new IllegalStateException("没有可用的 MCP Sync Client，请检查 sse 连接配置");
        }
        return clients.getFirst();
    }
}
