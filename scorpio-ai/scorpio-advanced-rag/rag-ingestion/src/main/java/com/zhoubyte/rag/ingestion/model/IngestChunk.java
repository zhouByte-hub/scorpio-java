package com.zhoubyte.rag.ingestion.model;

import java.util.Map;

/**
 * 入库 chunk（ES 文档结构）。
 *
 * <p>与 rag-pipeline 的索引约定兼容：contentTok（bigram，BM25 通道）、
 * sparse（rank_features，稀疏通道）、embedding（dense_vector，稠密通道），
 * 另外扩展了结构化元数据 blockType / sectionPath / page 用于溯源与过滤。</p>
 */
public record IngestChunk(
        String chunkId,
        String docId,
        String title,
        String sourceFile,
        String content,
        String contentTok,
        Map<String, Double> sparse,
        float[] embedding,
        String blockType,
        String sectionPath,
        Integer page,
        Integer position) {
}
