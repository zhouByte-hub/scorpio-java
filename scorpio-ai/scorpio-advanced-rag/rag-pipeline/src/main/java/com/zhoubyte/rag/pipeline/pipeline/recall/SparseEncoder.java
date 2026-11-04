package com.zhoubyte.rag.pipeline.pipeline.recall;

import java.util.Map;

/**
 * 查询侧稀疏向量编码器（SPLADE 风格：词项 -> 权重）。
 *
 * <p>本地 Ollama 无法产出 SPLADE / BGE-M3 稀疏表示，本实现采用近似方案：
 * 由 LLM 对查询做同义词/相关词扩展，再按语料 IDF 加权，模拟
 * “语义扩展 + 词项加权”的稀疏召回，弥补 BM25 纯字面匹配的短板。
 * 后续若部署 BGE-M3 服务，直接替换本接口实现即可，流水线其余部分不变。</p>
 */
public interface SparseEncoder {

    /**
     * 将查询编码为稀疏表示：bigram/词项 -> 正权重。
     */
    Map<String, Double> encode(String query);
}
