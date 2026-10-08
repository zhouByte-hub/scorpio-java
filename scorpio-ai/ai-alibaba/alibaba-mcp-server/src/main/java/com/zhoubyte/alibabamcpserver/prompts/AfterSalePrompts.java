package com.zhoubyte.alibabamcpserver.prompts;

import io.modelcontextprotocol.spec.McpSchema;
import org.springaicommunity.mcp.annotation.McpArg;
import org.springaicommunity.mcp.annotation.McpPrompt;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 电商售后 MCP Prompt 模板。
 *
 * <p>Prompt 是服务端预置的「提示词模板」：客户端按名称取回一组消息，
 * 直接作为对话输入交给自己的模型，实现提示词的集中管理与复用。</p>
 */
@Component
public class AfterSalePrompts {

    @McpPrompt(name = "after-sale-answer", title = "售后问答模板",
            description = "以售后专家身份回答用户问题，输出处理步骤与注意事项")
    public McpSchema.GetPromptResult afterSalePrompt(
            @McpArg(name = "question", description = "用户的售后问题", required = true) String question) {
        McpSchema.PromptMessage systemMessage = new McpSchema.PromptMessage(
                McpSchema.Role.USER,
                new McpSchema.TextContent("""
                        你是一位资深电商售后专家，请针对以下问题给出结构化的处理方案：\
                        先说明政策依据，再列出具体操作步骤，最后提示注意事项。

                        用户问题：%s""".formatted(question)));

        return new McpSchema.GetPromptResult("售后问答模板", List.of(systemMessage));
    }
}
