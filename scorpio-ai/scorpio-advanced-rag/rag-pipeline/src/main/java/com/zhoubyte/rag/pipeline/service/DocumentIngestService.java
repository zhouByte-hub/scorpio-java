package com.zhoubyte.rag.pipeline.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.DenseVectorSimilarity;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import com.zhoubyte.rag.pipeline.config.RagProperties;
import com.zhoubyte.rag.pipeline.model.DocumentChunk;
import com.zhoubyte.rag.pipeline.model.DocumentImportRequest;
import com.zhoubyte.rag.pipeline.model.MetadataInfo;
import com.zhoubyte.rag.pipeline.pipeline.preprocess.MetadataFilterResolver;
import com.zhoubyte.rag.pipeline.pipeline.recall.IdfWeighter;
import com.zhoubyte.rag.pipeline.util.CjkTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 文档导入服务：分块 → 三路索引结构构建 → 写入 ES → 登记元数据位图。
 *
 * <p>每个 chunk 同时构建三种可检索表示：
 * <ul>
 *   <li>contentTok：bigram 分词文本（BM25 通道）；</li>
 *   <li>sparse：IDF 加权词项（稀疏通道，rank_features）；</li>
 *   <li>embedding：Ollama 稠密向量（稠密通道，knn）。</li>
 * </ul>
 * 并为 chunk 分配全局唯一 seq：它是 RoaringBitmap 位图的下标，
 * 也是三路召回结果去重与 ES terms 过滤的统一主键。</p>
 */
@Service
public class DocumentIngestService {

    private static final Logger log = LoggerFactory.getLogger(DocumentIngestService.class);
    private static final int DEFAULT_CHUNK_SIZE = 300;
    private static final int DEFAULT_OVERLAP = 60;
    private static final int EMBED_BATCH = 8;

    private final ElasticsearchClient elasticsearchClient;
    private final EmbeddingModel embeddingModel;
    private final IdfWeighter idfWeighter;
    private final MetadataFilterResolver filterResolver;
    private final RagProperties properties;

    private AtomicLong seqCounter = new AtomicLong();

    public DocumentIngestService(ElasticsearchClient elasticsearchClient, EmbeddingModel embeddingModel,
                                 IdfWeighter idfWeighter, MetadataFilterResolver filterResolver,
                                 RagProperties properties) {
        this.elasticsearchClient = elasticsearchClient;
        this.embeddingModel = embeddingModel;
        this.idfWeighter = idfWeighter;
        this.filterResolver = filterResolver;
        this.properties = properties;
    }

    /**
     * 启动时确保索引存在，并以索引内现有文档数初始化 seq 计数器。
     */
    @Bean
    public ApplicationRunner ensureIndexRunner() {
        return args -> {
            ensureIndex();
            SearchResponse<Void> count = elasticsearchClient.search(
                    s -> s.index(properties.indexName()).size(0).trackTotalHits(t -> t.enabled(true)),
                    Void.class);
            long total = count.hits().total() == null ? 0 : count.hits().total().value();
            seqCounter = new AtomicLong(total);
            log.info("索引 {} 就绪，现有 chunk 数：{}", properties.indexName(), total);
        };
    }

    /**
     * 导入单篇文档，返回生成的 chunk 数。
     */
    public int ingest(DocumentImportRequest request) {
        String docId = "doc-" + java.util.UUID.randomUUID();
        int chunkSize = request.chunkSize() == null ? DEFAULT_CHUNK_SIZE : request.chunkSize();
        int overlap = request.overlap() == null ? DEFAULT_OVERLAP : request.overlap();
        MetadataInfo metadata = request.metadata() == null
                ? new MetadataInfo("", "public", "", "") : request.metadata();

        List<String> pieces = split(request.content(), chunkSize, overlap);
        List<DocumentChunk> chunks = new ArrayList<>(pieces.size());
        for (int i = 0; i < pieces.size(); i++) {
            long seq = seqCounter.incrementAndGet();
            String contentTok = CjkTokenizer.preTokenize(pieces.get(i));
            chunks.add(new DocumentChunk(docId + ":" + i, (int) seq, docId, request.title(),
                    pieces.get(i), contentTok, null, null, metadata, i + 1, i + 1));
        }

        // 分批向量化后写入 ES
        for (int from = 0; from < chunks.size(); from += EMBED_BATCH) {
            List<DocumentChunk> batch = chunks.subList(from, Math.min(from + EMBED_BATCH, chunks.size()));
            List<String> texts = batch.stream().map(DocumentChunk::content).toList();
            List<float[]> vectors = embeddingModel.embed(texts);
            for (int i = 0; i < batch.size(); i++) {
                DocumentChunk chunk = batch.get(i);
                DocumentChunk enriched = new DocumentChunk(chunk.chunkId(), chunk.seq(), chunk.docId(),
                        chunk.title(), chunk.content(), chunk.contentTok(),
                        idfWeighter.encodeIndex(chunk.contentTok()), vectors.get(i), chunk.metadata(),
                        chunk.page(), chunk.position());
                try {
                    elasticsearchClient.index(idx -> idx
                            .index(properties.indexName())
                            .id(enriched.chunkId())
                            .document(enriched));
                } catch (IOException e) {
                    throw new IllegalStateException("chunk 写入 ES 失败: " + enriched.chunkId(), e);
                }
                idfWeighter.onChunkIndexed(enriched.contentTok());
                filterResolver.onChunkIndexed(enriched.seq(), enriched.metadata());
            }
        }
        log.info("文档 {} 导入完成，标题：{}，chunk 数：{}", docId, request.title(), chunks.size());
        return chunks.size();
    }

    private void ensureIndex() {
        try {
            boolean exists = elasticsearchClient.indices().exists(e -> e.index(properties.indexName())).value();
            if (exists) {
                return;
            }
            TypeMapping mapping = TypeMapping.of(m -> m
                    .properties("chunkId", p -> p.keyword(k -> k))
                    .properties("seq", p -> p.integer(i -> i))
                    .properties("docId", p -> p.keyword(k -> k))
                    .properties("title", p -> p.text(t -> t))
                    .properties("content", p -> p.text(t -> t))
                    .properties("contentTok", p -> p.text(t -> t.analyzer("whitespace")))
                    .properties("sparse", p -> p.rankFeatures(r -> r))
                    .properties("embedding", p -> p.denseVector(d -> d
                            .dims(properties.embeddingDims()).index(true).similarity(DenseVectorSimilarity.Cosine)))
                    .properties("metadata", p -> p.object(o -> o
                            .properties("department", pp -> pp.keyword(k -> k))
                            .properties("level", pp -> pp.keyword(k -> k))
                            .properties("docType", pp -> pp.keyword(k -> k))
                            .properties("docDate", pp -> pp.keyword(k -> k))))
                    .properties("page", p -> p.integer(i -> i))
                    .properties("position", p -> p.integer(i -> i)));
            elasticsearchClient.indices().create(CreateIndexRequest.of(c -> c
                    .index(properties.indexName()).mappings(mapping)));
            log.info("已创建索引 {}", properties.indexName());
        } catch (IOException e) {
            throw new IllegalStateException("创建索引失败，请确认 Elasticsearch 已启动", e);
        }
    }

    /**
     * 滑动窗口分块：块长 chunkSize，相邻块重叠 overlap，保证语义边界处的信息不被切断。
     */
    private List<String> split(String content, int chunkSize, int overlap) {
        List<String> pieces = new ArrayList<>();
        String text = content == null ? "" : content.replaceAll("\\n{3,}", "\n\n").trim();
        if (text.isEmpty()) {
            return pieces;
        }
        int step = Math.max(chunkSize - overlap, 1);
        for (int start = 0; start < text.length(); start += step) {
            pieces.add(text.substring(start, Math.min(start + chunkSize, text.length())));
            if (start + chunkSize >= text.length()) {
                break;
            }
        }
        return pieces;
    }
}
