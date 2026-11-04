package com.zhoubyte.rag.pipeline.pipeline.preprocess;

import com.zhoubyte.rag.pipeline.util.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 【前置处理层】Query 改写：为同一个问题生成多个不同措辞的检索 Query。
 *
 * <p>用户的一个提问只覆盖一种表达方式，而文档作者的用词往往不同。
 * 改写生成同义/近义变体后送入多路召回，可显著提升召回覆盖率，
 * 由后续 RRF 融合消除多路结果之间的排名偏差。</p>
 */
@Component
public class QueryRewriter {

    private static final int VARIANTS = 2;

    private final ChatClient utilityClient;

    public QueryRewriter(ChatClient utilityClient) {
        this.utilityClient = utilityClient;
    }

    /**
     * 返回改写后的查询列表：原始查询在前，另生成若干同义变体。
     */
    public List<String> rewrite(String query) {
        String prompt = """
                你是检索查询改写器。针对以下查询，生成 %d 个语义相同但用词不同的检索变体，
                要求：覆盖同义词、不同表述习惯、可能出现在文档中的书面表达；每个变体一行短语，不要解释。
                只输出 JSON 字符串数组，如 ["变体1","变体2"]

                查询：%s""".formatted(VARIANTS, query);

        List<String> variants;
        try {
            variants = JsonUtils.parseArray(utilityClient.prompt().user(prompt).call().content(),
                    new TypeReference<List<String>>() {
                    });
        } catch (Exception e) {
            variants = List.of();
        }
        Set<String> queries = new LinkedHashSet<>();
        queries.add(query);
        variants.stream()
                .filter(v -> v != null && !v.isBlank())
                .map(String::trim)
                .forEach(queries::add);
        return new ArrayList<>(queries);
    }
}
