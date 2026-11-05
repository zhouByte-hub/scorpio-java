package com.zhoubyte.rag.ingestion.util;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 稀疏通道索引侧编码器：维护全局词项文档频率（DF），
 * 按 TF × IDF 为每个 chunk 生成 rank_features 加权词项。
 *
 * <p>IDF 随入库文档增加而动态更新，新 chunk 按最新 IDF 编码；
 * 词表与 bigram 分词一致，与 rag-pipeline 的稀疏召回通道约定兼容。</p>
 */
@Component
public class SparseEncoder {

    private final Map<String, Long> docFrequencies = new HashMap<>();
    private final AtomicLong totalDocs = new AtomicLong();

    /**
     * 为 chunk 生成稀疏向量（词项 → TF × IDF 权重）。
     */
    public Map<String, Double> encodeIndex(String contentTok) {
        if (contentTok == null || contentTok.isBlank()) {
            return Map.of();
        }
        Map<String, Integer> termFrequency = new HashMap<>();
        for (String token : contentTok.split(" ")) {
            termFrequency.merge(token, 1, Integer::sum);
        }
        long total = Math.max(totalDocs.get(), 1);
        Map<String, Double> sparse = new HashMap<>();
        for (Map.Entry<String, Integer> entry : termFrequency.entrySet()) {
            long df = docFrequencies.getOrDefault(entry.getKey(), 0L);
            double idf = Math.log((double) (total + 1) / (df + 1)) + 1.0;
            sparse.put(entry.getKey(), entry.getValue() * idf);
        }
        return sparse;
    }

    /**
     * chunk 入库后登记其词项，更新全局 DF。
     */
    public void onChunkIndexed(String contentTok) {
        if (contentTok == null || contentTok.isBlank()) {
            return;
        }
        Set<String> distinct = new HashSet<>();
        for (String token : contentTok.split(" ")) {
            distinct.add(token);
        }
        distinct.forEach(token -> docFrequencies.merge(token, 1L, Long::sum));
        totalDocs.incrementAndGet();
    }
}
