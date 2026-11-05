package com.zhoubyte.rag.ingestion.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.DenseVectorSimilarity;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import com.zhoubyte.rag.ingestion.block.ParsedDocument;
import com.zhoubyte.rag.ingestion.chunk.ChunkResult;
import com.zhoubyte.rag.ingestion.chunk.StructureAwareChunker;
import com.zhoubyte.rag.ingestion.config.RagIngestionProperties;
import com.zhoubyte.rag.ingestion.model.IngestChunk;
import com.zhoubyte.rag.ingestion.parser.ParserRouter;
import com.zhoubyte.rag.ingestion.util.CjkTokenizer;
import com.zhoubyte.rag.ingestion.util.SparseEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 文档入库编排：解析路由 → 结构感知分块 → 三路可检索表示构建 → 写入 ES 独立索引。
 */
@Service
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);
    private static final int EMBED_BATCH = 8;

    private final ElasticsearchClient elasticsearchClient;
    private final EmbeddingModel embeddingModel;
    private final SparseEncoder sparseEncoder;
    private final ParserRouter parserRouter;
    private final StructureAwareChunker chunker;
    private final RagIngestionProperties properties;

    public IngestionService(ElasticsearchClient elasticsearchClient, EmbeddingModel embeddingModel,
                            SparseEncoder sparseEncoder, ParserRouter parserRouter,
                            StructureAwareChunker chunker, RagIngestionProperties properties) {
        this.elasticsearchClient = elasticsearchClient;
        this.embeddingModel = embeddingModel;
        this.sparseEncoder = sparseEncoder;
        this.parserRouter = parserRouter;
        this.chunker = chunker;
        this.properties = properties;
    }

    /**
     * 启动时确保索引存在。
     */
    @Bean
    public ApplicationRunner ensureIndexRunner() {
        return args -> ensureIndex();
    }

    /**
     * 导入文件：解析 → 分块 → 向量化 → 入库。
     */
    public ImportResult ingest(byte[] data, String fileName, String titleOverride) {
        ParsedDocument parsed = parserRouter.parse(data, fileName);
        String title = titleOverride != null && !titleOverride.isBlank() ? titleOverride : parsed.title();
        List<ChunkResult> chunks = chunker.chunk(parsed);
        String docId = "doc-" + UUID.randomUUID();

        Map<String, Long> blockTypeCounts = new LinkedHashMap<>();
        for (int i = 0; i < chunks.size(); i++) {
            ChunkResult chunk = chunks.get(i);
            String contentTok = CjkTokenizer.preTokenize(chunk.content());
            IngestChunk ingestChunk = new IngestChunk(docId + ":" + i, docId, title, fileName,
                    chunk.content(), contentTok, sparseEncoder.encodeIndex(contentTok), null,
                    chunk.blockType(), chunk.sectionPath(), chunk.page(), chunk.position());
            blockTypeCounts.merge(chunk.blockType(), 1L, Long::sum);
            indexChunk(ingestChunk);
        }
        log.info("文档入库完成，docId：{}，标题：{}，chunk：{}，类型分布：{}",
                docId, title, chunks.size(), blockTypeCounts);
        return new ImportResult(docId, title, fileName, chunks.size(), blockTypeCounts);
    }

    /**
     * 预览：只解析与分块，不写 ES、不做向量化，用于调参观察切分效果。
     */
    public PreviewResult preview(byte[] data, String fileName, String titleOverride) {
        ParsedDocument parsed = parserRouter.parse(data, fileName);
        String title = titleOverride != null && !titleOverride.isBlank() ? titleOverride : parsed.title();
        List<ChunkResult> chunks = chunker.chunk(parsed);
        return new PreviewResult(title, fileName, chunks.size(), chunks);
    }

    /**
     * 按 docId 查询全部 chunk（按 position 升序）。
     */
    public List<IngestChunk> findByDocId(String docId) {
        try {
            SearchResponse<IngestChunk> response = elasticsearchClient.search(s -> s
                            .index(properties.indexName())
                            .size(500)
                            .query(q -> q.term(t -> t.field("docId").value(docId)))
                            .sort(so -> so.field(f -> f.field("position").order(co.elastic.clients.elasticsearch._types.SortOrder.Asc))),
                    IngestChunk.class);
            return response.hits().hits().stream().map(h -> h.source()).toList();
        } catch (IOException e) {
            throw new IllegalStateException("查询 chunk 失败: " + docId, e);
        }
    }

    private void indexChunk(IngestChunk chunk) {
        // 表格/图片块内容参与向量化与稀疏编码，正文已经含章节路径前缀
        List<float[]> vectors = embeddingModel.embed(List.of(chunk.content()));
        IngestChunk enriched = new IngestChunk(chunk.chunkId(), chunk.docId(), chunk.title(),
                chunk.sourceFile(), chunk.content(), chunk.contentTok(), chunk.sparse(),
                vectors.getFirst(), chunk.blockType(), chunk.sectionPath(), chunk.page(), chunk.position());
        try {
            elasticsearchClient.index(idx -> idx
                    .index(properties.indexName())
                    .id(enriched.chunkId())
                    .document(enriched));
        } catch (IOException e) {
            throw new IllegalStateException("chunk 写入 ES 失败: " + enriched.chunkId(), e);
        }
        sparseEncoder.onChunkIndexed(enriched.contentTok());
    }

    private void ensureIndex() {
        try {
            boolean exists = elasticsearchClient.indices().exists(e -> e.index(properties.indexName())).value();
            if (exists) {
                return;
            }
            TypeMapping mapping = TypeMapping.of(m -> m
                    .properties("chunkId", p -> p.keyword(k -> k))
                    .properties("docId", p -> p.keyword(k -> k))
                    .properties("title", p -> p.text(t -> t))
                    .properties("sourceFile", p -> p.keyword(k -> k))
                    .properties("content", p -> p.text(t -> t))
                    .properties("contentTok", p -> p.text(t -> t.analyzer("whitespace")))
                    .properties("sparse", p -> p.rankFeatures(r -> r))
                    .properties("embedding", p -> p.denseVector(d -> d
                            .dims(properties.embeddingDims()).index(true).similarity(DenseVectorSimilarity.Cosine)))
                    .properties("blockType", p -> p.keyword(k -> k))
                    .properties("sectionPath", p -> p.keyword(k -> k))
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
     * 导入结果。
     */
    public record ImportResult(String docId, String title, String sourceFile, int chunkCount,
                               Map<String, Long> blockTypeCounts) {
    }

    /**
     * 预览结果。
     */
    public record PreviewResult(String title, String sourceFile, int chunkCount, List<ChunkResult> chunks) {
    }
}
