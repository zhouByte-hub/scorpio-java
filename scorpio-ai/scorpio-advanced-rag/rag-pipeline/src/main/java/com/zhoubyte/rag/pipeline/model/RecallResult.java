package com.zhoubyte.rag.pipeline.model;

/**
 * 单路召回结果。
 *
 * @param chunkSeq 命中的 chunk 序号
 * @param score    该通道的原始得分
 * @param channel  通道名：bm25 / sparse / dense
 * @param query    产生该结果的检索 query（改写后）
 */
public record RecallResult(int chunkSeq, double score, String channel, String query) {
}
