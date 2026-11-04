package com.zhoubyte.rag.pipeline.pipeline.recall;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.zhoubyte.rag.pipeline.config.RagProperties;
import com.zhoubyte.rag.pipeline.model.DocumentChunk;
import com.zhoubyte.rag.pipeline.model.RecallResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 【多路召回 C】Dense 稠密向量召回（Ollama embedding + ES knn）。
 *
 * <p>对口语化、指代模糊的问句效果最好；配置 scorpio.rag.dense-enabled=false 可关闭，
 * 与方案中“可选、不强求、后期开启”一致。</p>
 */
@Component
@ConditionalOnProperty(prefix = "scorpio.rag", name = "dense-enabled", havingValue = "true", matchIfMissing = true)
public class DenseRecallChannel implements RecallChannel {

    private static final Logger log = LoggerFactory.getLogger(DenseRecallChannel.class);

    private final ElasticsearchClient elasticsearchClient;
    private final EmbeddingModel embeddingModel;
    private final RagProperties properties;

    public DenseRecallChannel(ElasticsearchClient elasticsearchClient, EmbeddingModel embeddingModel,
                              RagProperties properties) {
        this.elasticsearchClient = elasticsearchClient;
        this.embeddingModel = embeddingModel;
        this.properties = properties;
    }

    @Override
    public String name() {
        return "dense";
    }

    @Override
    public List<RecallResult> recall(String query, Optional<Set<Integer>> allowed, int topK) {
        float[] vector = embeddingModel.embed(query);
        List<Float> queryVector = new ArrayList<>(vector.length);
        for (float v : vector) {
            queryVector.add(v);
        }
        Query knnQuery = Query.of(q -> q.knn(knn -> {
            knn.field("embedding")
                    .queryVector(queryVector)
                    .k(topK)
                    .numCandidates(Math.max(topK * 3, 100));
            allowed.ifPresent(seqs -> knn.filter(Bm25RecallChannel.termsQuery(seqs)));
            return knn;
        }));
        try {
            SearchResponse<DocumentChunk> response = elasticsearchClient.search(
                    s -> s.index(properties.indexName()).query(knnQuery).size(0),
                    DocumentChunk.class);
            return response.hits().hits().stream()
                    .filter(hit -> hit.source() != null && hit.score() != null)
                    .map(hit -> new RecallResult(hit.source().seq(), hit.score(), name(), ""))
                    .toList();
        } catch (IOException e) {
            log.warn("稠密召回失败: {}", e.getMessage());
            return List.of();
        }
    }
}
