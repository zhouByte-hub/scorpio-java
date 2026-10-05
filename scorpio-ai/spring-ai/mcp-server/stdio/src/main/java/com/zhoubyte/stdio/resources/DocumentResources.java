package com.zhoubyte.stdio.resources;

import org.springframework.ai.mcp.annotation.McpArg;
import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.stereotype.Component;

@Component
public class DocumentResources {

    @McpResource(
            uri = "doc://{docId}",
            name = "文档资源",
            description = "读取指定文档片段",
            mimeType = "text/plain"
    )
    public String loadDoc(@McpArg(description = "文档ID", required = true) String docId) {
        return "文档[" + docId + "] 内容：Spring AI MCP STDIO Demo";
    }
}
