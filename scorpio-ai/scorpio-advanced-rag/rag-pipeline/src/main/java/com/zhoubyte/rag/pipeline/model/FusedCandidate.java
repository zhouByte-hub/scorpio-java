package com.zhoubyte.rag.pipeline.model;

import java.util.Map;

/**
 * RRF 融合后的候选 chunk。
 *
 * @param chunkSeq     chunk 序号
 * @param score        融合得分（RRF 分数 sum(1/(k+rank))；经重排后语义变为 LLM 相关性得分 0~10）
 * @param channelRanks 各通道中的排名（通道名 -> 排名），用于溯源观测
 */
public record FusedCandidate(int chunkSeq, double score, Map<String, Integer> channelRanks) {
}
