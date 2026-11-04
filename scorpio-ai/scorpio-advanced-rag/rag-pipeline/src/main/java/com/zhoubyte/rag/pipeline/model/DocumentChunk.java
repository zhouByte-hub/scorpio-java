package com.zhoubyte.rag.pipeline.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

/**
 * ES 索引文档（rag_chunks）：一个 chunk 即一条可溯源的检索单元。
 *
 * @param chunkId    chunk 唯一 ID（docId:index 形式）
 * @param seq        递增整数，RoaringBitmap 位图中的位置，同时作为 ES terms 过滤键
 * @param docId      所属文档 ID
 * @param title      文档标题
 * @param content    原文内容
 * @param contentTok 分词后内容（bigram 空格拼接，whitespace 分词器 + BM25）
 * @param sparse     稀疏向量：词项 -> 权重（rank_features 字段）
 * @param embedding  稠密向量（dense_vector + knn）
 * @param metadata   文档元数据
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DocumentChunk(
        String chunkId,
        int seq,
        String docId,
        String title,
        String content,
        String contentTok,
        Map<String, Double> sparse,
        float[] embedding,
        MetadataInfo metadata,
        int page,
        int position) {
}
