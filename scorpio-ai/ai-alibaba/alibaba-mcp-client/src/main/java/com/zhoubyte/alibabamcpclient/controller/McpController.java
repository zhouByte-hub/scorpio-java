package com.zhoubyte.alibabamcpclient.controller;

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

/**
 * MCP Client 演示接口：远程 Tool 调用、Resource 读取、Prompt 模板获取。
 */
@RestController
@RequestMapping("/mcp")
public class McpController {

    private final ObjectProvider<List<McpSyncClient>> mcpSyncClients;

    private final ChatClient ollamaChatClient;

    public McpController(ObjectProvider<List<McpSyncClient>> mcpSyncClients,
                         @Qualifier("ollamaChatClient") ChatClient ollamaChatClient) {
        this.mcpSyncClients = mcpSyncClients;
        this.ollamaChatClient = ollamaChatClient;
    }

    /**
     * 远程 Tool 调用：模型自动选择 MCP Server 的 query_order / query_logistics。
     * 示例：/mcp/chat?question=帮我查一下订单SO20261001的物流到哪了
     */
    @GetMapping("/chat")
    public String chat(@RequestParam String question) {
        return ollamaChatClient.prompt()
                .user(question)
                .call()
                .content();
    }

    /**
     * 读取 MCP Server 资源（商品详情）。
     * 示例：/mcp/resource?uri=product://1001
     */
    @GetMapping("/resource")
    public String readResource(@RequestParam("uri") String uri) {
        McpSchema.ReadResourceResult result = mcpClient()
                .readResource(new McpSchema.ReadResourceRequest(uri));
        return result.contents().stream()
                .filter(McpSchema.TextResourceContents.class::isInstance)
                .map(content -> ((McpSchema.TextResourceContents) content).text())
                .collect(Collectors.joining("\n"));
    }

    /**
     * 获取 MCP Server 的 Prompt 模板（after-sale-answer），并把渲染结果交给模型回答。
     * 示例：/mcp/prompt?question=耳机用了三天就坏了能退换吗
     */
    @GetMapping("/prompt")
    public String prompt(@RequestParam("question") String question) {
        McpSchema.GetPromptResult result = mcpClient()
                .getPrompt(new McpSchema.GetPromptRequest("after-sale-answer",
                        Map.of("question", question)));
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
            throw new IllegalStateException("没有可用的 MCP Sync Client，请检查 streamable-http 连接配置");
        }
        return clients.getFirst();
    }
}
