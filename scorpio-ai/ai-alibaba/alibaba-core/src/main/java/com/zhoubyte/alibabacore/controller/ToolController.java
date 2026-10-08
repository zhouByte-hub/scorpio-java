package com.zhoubyte.alibabacore.controller;

import com.zhoubyte.alibabacore.tool.OrderTools;
import com.zhoubyte.alibabacore.tool.WeatherTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tool（函数）调用示例：模型自主决定何时调用哪个工具。
 */
@RestController
@RequestMapping("/tool")
public class ToolController {

    private final ChatClient chatClient;
    private final WeatherTools weatherTools;
    private final OrderTools orderTools;

    public ToolController(ChatClient chatClient, WeatherTools weatherTools, OrderTools orderTools) {
        this.chatClient = chatClient;
        this.weatherTools = weatherTools;
        this.orderTools = orderTools;
    }

    /**
     * 注册工具后提问，模型会按需调用 getWeather / queryOrder 等。
     * 示例：/tool/chat?question=杭州今天天气怎么样，适合出门吗
     * 示例：/tool/chat?question=帮我查一下订单SO20261001到哪了
     */
    @GetMapping("/chat")
    public String chat(@RequestParam String question) {
        return chatClient.prompt()
                .user(question)
                // tools(Object...) 会把对象中标注 @Tool 的方法全部注册给模型
                .tools(weatherTools, orderTools)
                .call()
                .content();
    }
}
