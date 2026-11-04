package com.zhoubyte.rag.pipeline.pipeline.recall;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.zhoubyte.rag.pipeline.config.RagProperties;
import com.zhoubyte.rag.pipeline.model.DocumentChunk;
import com.zhoubyte.rag.pipeline.model.RecallResult;
import com.zhoubyte.rag.pipeline.util.CjkTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 【多路召回 A】BM25 倒排召回（ES 原生 match）。
 *
 * <p>零 Embedding、纯词频统计，专攻专有名词、编号、原文精确匹配，
 * 保证“硬信息”（型号、条款号、人名等）不漏检。
 * 索引与查询两侧统一使用 {@link com.zhoubyte.rag.pipeline.util.CjkTokenizer}
 * 产出的 bigram 词项（whitespace 分词器），本地环境无需部署 IK 插件。</p>
 */
@Component
public class Bm25RecallChannel implements RecallChannel {

    private static final Logger log = LoggerFactory.getLogger(Bm25RecallChannel.class);

    private final ElasticsearchClient elasticsearchClient;
    private final RagProperties properties;

    public Bm25RecallChannel(ElasticsearchClient elasticsearchClient, RagProperties properties) {
        this.elasticsearchClient = elasticsearchClient;
        this.properties = properties;
    }

    @Override
    public String name() {
        return "bm25";
    }

    @Override
    public List<RecallResult> recall(String query, Optional<Set<Integer>> allowed, int topK) {
        String tokenized = CjkTokenizer.preTokenize(query);
        Query esQuery = Query.of(q -> q.bool(b -> {
            b.must(m -> m.match(match -> match
                    .field("contentTok")
                    .query(tokenized)
                    .analyzer("whitespace")));
            allowed.ifPresent(seqs -> b.filter(termsQuery(seqs)));
            return b;
        }));
        return search(esQuery, topK);
    }

    private List<RecallResult> search(Query esQuery, int topK) {
        try {
            SearchResponse<DocumentChunk> response = elasticsearchClient.search(
                    s -> s.index(properties.indexName()).query(esQuery).size(topK), DocumentChunk.class);
            return response.hits().hits().stream()
                    .filter(hit -> hit.source() != null)
                    .map(this::toResult)
                    .toList();
        } catch (IOException e) {
            log.warn("BM25 召回失败: {}", e.getMessage());
            return List.of();
        }
    }

    private RecallResult toResult(Hit<DocumentChunk> hit) {
        return new RecallResult(hit.source().seq(), hit.score() == null ? 0 : hit.score(), name(), "");
    }

    /**
     * seq 白名单 terms 过滤条件（RoaringBitmap 解析结果下推到 ES）。
     */
    static Query termsQuery(Set<Integer> seqs) {
        return Query.of(q -> q.terms(t -> t.field("seq").terms(tv -> tv.value(
                seqs.stream().map(FieldValue::of).toList()))));
    }
}
