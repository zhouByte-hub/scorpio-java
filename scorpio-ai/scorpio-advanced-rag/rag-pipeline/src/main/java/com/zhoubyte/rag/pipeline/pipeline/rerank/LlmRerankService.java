package com.zhoubyte.rag.pipeline.pipeline.rerank;

import com.zhoubyte.rag.pipeline.config.RagProperties;
import com.zhoubyte.rag.pipeline.model.DocumentChunk;
import com.zhoubyte.rag.pipeline.model.FusedCandidate;
import com.zhoubyte.rag.pipeline.util.JsonUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 【重排】LLM 精细相关性重排。
 *
 * <p>召回阶段的分数是“字面/向量相似度”，不等于“相关性”——
 * 语义相似但答非所问的垃圾 chunk 往往在这一层才暴露。
 * 由 LLM 对每个候选按与查询的真实相关性打 0~10 分，
 * 低于阈值的直接剔除，只保留前 N 条进入上下文。
 * 生产环境可将本组件替换为专业 Rerank 模型（如 gte-rerank），接口不变。</p>
 */
@Component
public class LlmRerankService {

    private static final int SNIPPET_LENGTH = 400;

    private final ChatClient utilityClient;
    private final RagProperties properties;

    public LlmRerankService(ChatClient utilityClient, RagProperties properties) {
        this.utilityClient = utilityClient;
        this.properties = properties;
    }

    /**
     * 重排：候选按 LLM 相关性得分降序，低于阈值剔除，保留 topN。
     * LLM 输出异常时降级为直接保留 RRF 前 N 条（不阻断流水线）。
     */
    public List<FusedCandidate> rerank(String query, List<FusedCandidate> candidates,
                                       Map<Integer, DocumentChunk> chunkMap) {
        if (candidates.isEmpty()) {
            return candidates;
        }
        StringBuilder listing = new StringBuilder();
        for (int i = 0; i < candidates.size(); i++) {
            DocumentChunk chunk = chunkMap.get(candidates.get(i).chunkSeq());
            String content = chunk == null ? "" : chunk.content();
            if (content.length() > SNIPPET_LENGTH) {
                content = content.substring(0, SNIPPET_LENGTH) + "…";
            }
            String title = chunk == null ? "" : chunk.title();
            listing.append("[id=%d]《%s》%s\n".formatted(i + 1, title, content));
        }

        Map<Integer, Double> scores = new HashMap<>();
        try {
            String prompt = """
                    你是搜索相关性重排模型。给定查询和 %d 个候选片段，
                    对每个片段输出 0~10 的相关性整数得分：10=直接回答查询，0=完全无关。
                    只依据片段内容判断，不要臆测。只输出 JSON 数组：[{"id":1,"score":8},...]

                    查询：%s

                    候选片段：
                    %s""".formatted(candidates.size(), query, listing);
            String output = utilityClient.prompt().user(prompt).call().content();
            JsonUtils.parseArray(output, new com.fasterxml.jackson.core.type.TypeReference<List<Score>>() {
                    })
                    .forEach(score -> scores.put(score.id(), Math.max(0, Math.min(10, score.score()))));
        } catch (Exception e) {
            // 降级：LLM 打分失败时保留 RRF 顺序
            return candidates.stream()
                    .limit(properties.rerankTopN())
                    .toList();
        }

        List<FusedCandidate> reranked = candidates.stream()
                .filter(c -> scores.getOrDefault(indexOf(candidates, c), 0.0) >= properties.rerankThreshold())
                .map(c -> new FusedCandidate(c.chunkSeq(),
                        scores.getOrDefault(indexOf(candidates, c), 0.0), c.channelRanks()))
                .sorted(Comparator.comparingDouble(FusedCandidate::score).reversed())
                .limit(properties.rerankTopN())
                .toList();
        return reranked;
    }

    private int indexOf(List<FusedCandidate> candidates, FusedCandidate target) {
        for (int i = 0; i < candidates.size(); i++) {
            if (candidates.get(i).chunkSeq() == target.chunkSeq()) {
                return i + 1;
            }
        }
        return 0;
    }

    /**
     * LLM 打分条目。
     */
    public record Score(int id, double score) {
    }
}
