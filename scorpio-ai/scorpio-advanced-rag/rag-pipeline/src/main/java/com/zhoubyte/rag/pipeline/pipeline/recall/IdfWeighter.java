package com.zhoubyte.rag.pipeline.pipeline.recall;

import com.zhoubyte.rag.pipeline.util.CjkTokenizer;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 语料级 IDF 统计与词项加权（索引侧与查询侧共用）。
 *
 * <p>索引时为每个 chunk 的分词结果累加文档频率 df；权重公式：
 * {@code tf * (ln(N / df) + 1)}，与 SPLADE/BGE-M3 的稀疏权重同构：
 * 词项在语料中越稀有、在本文档中出现越多，权重越高。
 * 注意：df 统计保存在内存中，重启后随再次导入自动重建；生产环境应持久化。</p>
 */
@Component
public class IdfWeighter {

    private final Map<String, AtomicLong> docFrequency = new ConcurrentHashMap<>();
    private final AtomicLong totalDocs = new AtomicLong();

    /**
     * chunk 入库时累加文档频率。
     */
    public void onChunkIndexed(String tokenizedContent) {
        totalDocs.incrementAndGet();
        CjkTokenizer.tokenize(tokenizedContent).stream()
                .distinct()
                .forEach(token -> docFrequency.computeIfAbsent(token, k -> new AtomicLong()).incrementAndGet());
    }

    /**
     * 索引侧编码：为分词后的 chunk 文本计算稀疏权重。
     */
    public Map<String, Double> encodeIndex(String tokenizedContent) {
        Map<String, Double> sparse = new HashMap<>();
        if (tokenizedContent == null || tokenizedContent.isBlank()) {
            return sparse;
        }
        List<String> tokens = CjkTokenizer.tokenize(tokenizedContent);
        Map<String, Long> tf = new HashMap<>();
        tokens.forEach(token -> tf.merge(token, 1L, Long::sum));
        tf.forEach((token, count) -> sparse.put(token, count * (idf(token) + 1)));
        return sparse;
    }

    /**
     * 词项的逆文档频率：ln(N / df)。
     */
    public double idf(String token) {
        long df = docFrequency.getOrDefault(token, new AtomicLong()).get();
        long n = totalDocs.get();
        if (df == 0 || n == 0) {
            return 1.0;
        }
        return Math.log((double) n / df);
    }
}
