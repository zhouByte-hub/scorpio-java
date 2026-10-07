package com.zhoubyte.stream.prompts;

import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.mcp.annotation.McpArg;
import org.springframework.ai.mcp.annotation.McpPrompt;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PrePromptTemplate {

    @McpPrompt(name = "expert-answer", description = "生成专家风格回答的prompt模板")
    public McpSchema.GetPromptResult expertPrompt(
            @McpArg(name = "question", description = "用户问题", required = true) String question
    ) {
        McpSchema.PromptMessage promptMessage = McpSchema.PromptMessage
                .builder(
                        McpSchema.Role.USER,
                        McpSchema.TextContent.builder("请以专家身份回答：" + question).build()
                )
                .build();

        return McpSchema.GetPromptResult.builder(List.of(promptMessage))
                .description("专家问答模板")
                .build();
    }
}
