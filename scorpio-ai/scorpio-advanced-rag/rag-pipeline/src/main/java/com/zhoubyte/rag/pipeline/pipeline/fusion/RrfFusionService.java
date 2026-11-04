package com.zhoubyte.rag.pipeline.pipeline.fusion;

import com.zhoubyte.rag.pipeline.model.FusedCandidate;
import com.zhoubyte.rag.pipeline.model.RecallResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 【融合】RRF（Reciprocal Rank Fusion）倒数排名融合。
 *
 * <p>BM25 分数与向量相似度、rank_features 权重的量纲完全不同，不能直接相加。
 * RRF 只看每一路内部的排名：{@code score(d) = Σ 1 / (k + rank(d))}，
 * 量纲无关、实现简单，被大量研究证明优于加权归一化方案。
 * 多个改写 Query 的结果也在本步完成合并与去重。</p>
 */
@Service
public class RrfFusionService {

    /**
     * @param channelResults 各路召回结果（外层为通道/Query，内层为该路的有序候选）
     * @param rrfK           平滑常数，常用 60
     * @param limit          融合后保留的候选数
     */
    public List<FusedCandidate> fuse(List<List<RecallResult>> channelResults, int rrfK, int limit) {
        Map<Integer, Double> scores = new HashMap<>();
        Map<Integer, Map<String, Integer>> ranks = new HashMap<>();

        for (List<RecallResult> results : channelResults) {
            List<RecallResult> sorted = results.stream()
                    .sorted(Comparator.comparingDouble(RecallResult::score).reversed())
                    .toList();
            for (int i = 0; i < sorted.size(); i++) {
                RecallResult result = sorted.get(i);
                scores.merge(result.chunkSeq(), 1.0 / (rrfK + i + 1), Double::sum);
                // 同一 chunk 在同一通道多次命中时保留最好名次
                ranks.computeIfAbsent(result.chunkSeq(), k -> new HashMap<>())
                        .putIfAbsent(result.channel(), i + 1);
            }
        }

        List<FusedCandidate> candidates = new ArrayList<>();
        scores.forEach((seq, score) -> candidates.add(new FusedCandidate(seq, score, ranks.get(seq))));
        candidates.sort(Comparator.comparingDouble(FusedCandidate::score).reversed());
        return candidates.size() > limit ? candidates.subList(0, limit) : candidates;
    }
}
