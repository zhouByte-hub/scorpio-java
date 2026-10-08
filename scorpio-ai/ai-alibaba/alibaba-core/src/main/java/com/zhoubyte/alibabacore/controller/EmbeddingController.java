package com.zhoubyte.alibabacore.controller;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Embedding（文本向量化）示例。
 *
 * <p>EmbeddingModel 由 spring-ai-starter-model-ollama 根据
 * spring.ai.ollama.embedding.model 自动装配（此处为 nomic-embed-text）。
 * 向量可用于语义检索、相似度匹配、RAG 知识库等场景。</p>
 */
@RestController
@RequestMapping("/embedding")
public class EmbeddingController {

    private final EmbeddingModel embeddingModel;

    public EmbeddingController(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    /**
     * 文本向量化：返回向量维度与前 8 个分量。
     * 示例：/embedding/encode?text=春天来了
     */
    @GetMapping("/encode")
    public Map<String, Object> encode(@RequestParam String text) {
        float[] vector = embeddingModel.embed(text);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("text", text);
        result.put("model", embeddingModel.getClass().getSimpleName());
        result.put("dimensions", vector.length);
        result.put("preview", Arrays.stream(vectorToDoubles(vector)).limit(8).toArray());
        return result;
    }

    /**
     * 语义相似度：余弦相似度越接近 1 表示语义越相近。
     * 示例：/embedding/similarity?text1=我喜欢看书&text2=阅读是我的爱好
     */
    @GetMapping("/similarity")
    public Map<String, Object> similarity(@RequestParam String text1, @RequestParam String text2) {
        float[] v1 = embeddingModel.embed(text1);
        float[] v2 = embeddingModel.embed(text2);
        double score = cosineSimilarity(v1, v2);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("text1", text1);
        result.put("text2", text2);
        result.put("cosineSimilarity", Math.round(score * 10000) / 10000.0);
        return result;
    }

    private double cosineSimilarity(float[] a, float[] b) {
        double dot = 0;
        double normA = 0;
        double normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += (double) a[i] * b[i];
            normA += (double) a[i] * a[i];
            normB += (double) b[i] * b[i];
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private double[] vectorToDoubles(float[] vector) {
        double[] doubles = new double[vector.length];
        for (int i = 0; i < vector.length; i++) {
            doubles[i] = vector[i];
        }
        return doubles;
    }
}
