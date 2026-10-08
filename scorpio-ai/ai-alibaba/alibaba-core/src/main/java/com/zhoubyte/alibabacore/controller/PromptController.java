package com.zhoubyte.alibabacore.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Prompt 模板示例。
 *
 * <p>Spring AI 的模板默认使用 StringTemplate（{占位符} 语法），
 * 底层由 spring-ai-template-st 提供渲染引擎。</p>
 */
@RestController
@RequestMapping("/prompt")
public class PromptController {

    private final ChatClient chatClient;

    public PromptController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 纯模板渲染：PromptTemplate 只负责把变量填充进模板文本，不调用模型。
     * 示例：/prompt/template?city=杭州&season=秋天
     */
    @GetMapping("/template")
    public String template(@RequestParam String city, @RequestParam String season) {
        PromptTemplate template = new PromptTemplate(
                "请你用两句话描述{city}的{season}景色，要求语言优美。");
        return template.render(Map.of("city", city, "season", season));
    }

    /**
     * 模板 + 模型：ChatClient 的 user/system 文本同样支持 {占位符}，
     * 通过 param() 注入变量后由框架渲染并发送给模型。
     * 示例：/prompt/travel?city=西安&days=3
     */
    @GetMapping("/travel")
    public String travel(@RequestParam String city, @RequestParam int days) {
        return chatClient.prompt()
                .system(s -> s.text("你是一位资深{role}旅行规划师，熟悉{city}当地风土人情。")
                        .param("role", "中文")
                        .param("city", city))
                .user(u -> u.text("请为我规划一份{days}天的{city}旅行行程，按天列出景点与美食。")
                        .param("days", days)
                        .param("city", city))
                .call()
                .content();
    }
}
