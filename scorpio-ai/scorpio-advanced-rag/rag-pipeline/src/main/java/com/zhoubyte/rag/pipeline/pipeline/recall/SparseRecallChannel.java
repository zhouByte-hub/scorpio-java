package com.zhoubyte.rag.pipeline.pipeline.recall;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.zhoubyte.rag.pipeline.config.RagProperties;
import com.zhoubyte.rag.pipeline.model.DocumentChunk;
import com.zhoubyte.rag.pipeline.model.RecallResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 【多路召回 B】稀疏 Embedding 召回（ES rank_features 字段 + terms 权重求和）。
 *
 * <p>查询侧由 {@link SparseEncoder}（LLM 同义扩展 + IDF 加权）产出词项权重，
 * 索引侧存储了每个 chunk 的加权词项。terms 查询作用在 rank_features 字段上时，
 * 得分为命中词项的权重之和，多词项一次检索即可完成“语义同义但字面不同”
 * 的扩展匹配，弥补 BM25 的字面匹配短板。</p>
 */
@Component
public class SparseRecallChannel implements RecallChannel {

    private static final Logger log = LoggerFactory.getLogger(SparseRecallChannel.class);
    private static final int MAX_TERMS = 64;

    private final ElasticsearchClient elasticsearchClient;
    private final SparseEncoder sparseEncoder;
    private final RagProperties properties;

    public SparseRecallChannel(ElasticsearchClient elasticsearchClient, SparseEncoder sparseEncoder,
                               RagProperties properties) {
        this.elasticsearchClient = elasticsearchClient;
        this.sparseEncoder = sparseEncoder;
        this.properties = properties;
    }

    @Override
    public String name() {
        return "sparse";
    }

    @Override
    public List<RecallResult> recall(String query, Optional<Set<Integer>> allowed, int topK) {
        Map<String, Double> sparse = sparseEncoder.encode(query);
        if (sparse.isEmpty()) {
            return List.of();
        }
        // 权重降序取前 N 个词项，避免词项爆炸拖垮查询；terms 查询作用于 rank_features
        // 字段时，得分为命中词项权重之和（客户端 8.18 已移除 rankFeatures 查询变体）
        List<FieldValue> termValues = sparse.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(MAX_TERMS)
                .map(entry -> FieldValue.of(entry.getKey()))
                .toList();

        Query esQuery = Query.of(q -> q.bool(b -> {
            b.should(s -> s.terms(t -> t.field("sparse").terms(tv -> tv.value(termValues))));
            b.minimumShouldMatch("1");
            allowed.ifPresent(seqs -> b.filter(Bm25RecallChannel.termsQuery(seqs)));
            return b;
        }));

        try {
            SearchResponse<DocumentChunk> response = elasticsearchClient.search(
                    s -> s.index(properties.indexName()).query(esQuery).size(topK), DocumentChunk.class);
            return response.hits().hits().stream()
                    .filter(hit -> hit.source() != null && hit.score() != null && hit.score() > 0)
                    .map(hit -> new RecallResult(hit.source().seq(), hit.score(), name(), ""))
                    .toList();
        } catch (IOException e) {
            log.warn("稀疏召回失败: {}", e.getMessage());
            return List.of();
        }
    }
}
