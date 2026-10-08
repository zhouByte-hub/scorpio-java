package com.zhoubyte.alibabacore.controller;

import com.zhoubyte.alibabacore.model.MovieRecommendation;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 结构化输出（Structured Output）示例。
 *
 * <p>entity() 会在后台把目标类型的 JSON Schema 追加到提示词中，
 * 要求模型输出合法 JSON，并自动反序列化为 Java 对象，免去手工解析。</p>
 */
@RestController
@RequestMapping("/structured")
public class StructuredController {

    private final ChatClient chatClient;

    public StructuredController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 单对象输出：把模型回答直接映射为 MovieRecommendation 记录。
     * 示例：/structured/movie?genre=科幻
     */
    @GetMapping("/movie")
    public MovieRecommendation movie(@RequestParam String genre) {
        return chatClient.prompt()
                .user("请推荐一部" + genre + "题材的经典电影。")
                .call()
                .entity(MovieRecommendation.class);
    }

    /**
     * 列表输出：泛型包装返回多个对象。
     * 示例：/structured/movies?genre=悬疑
     */
    @GetMapping("/movies")
    public List<MovieRecommendation> movies(@RequestParam String genre) {
        return chatClient.prompt()
                .user("请推荐两部" + genre + "题材的经典电影。")
                .call()
                .entity(new ParameterizedTypeReference<List<MovieRecommendation>>() {
                });
    }
}
