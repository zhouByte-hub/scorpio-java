package com.zhoubyte.rag.ingestion.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * 文档解析入库可调参数，见 application.yaml 中 scorpio.ingestion.* 配置。
 *
 * @param indexName            Elasticsearch 索引名（独立于 rag-pipeline）
 * @param chunkSize            TEXT 块递归分割的目标块长（字符）
 * @param overlap              相邻 TEXT 块的重叠字符数
 * @param maxTableChars        表格块最大保留字符数，超出截断
 * @param vlmModel             图片描述使用的 Ollama 视觉模型
 * @param doclingBaseUrl       Docling 服务地址（PDF 复杂版式解析）
 * @param doclingTimeoutSeconds Docling 请求超时秒数
 * @param embeddingDims        稠密向量维度（nomic-embed-text 为 768）
 * @param es                   Elasticsearch 连接配置
 */
@ConfigurationProperties(prefix = "scorpio.ingestion")
public record RagIngestionProperties(
        @DefaultValue("rag_ingestion_chunks") String indexName,
        @DefaultValue("500") int chunkSize,
        @DefaultValue("50") int overlap,
        @DefaultValue("6000") int maxTableChars,
        @DefaultValue("qwen2.5vl:3b") String vlmModel,
        @DefaultValue("http://localhost:5001") String doclingBaseUrl,
        @DefaultValue("120") int doclingTimeoutSeconds,
        @DefaultValue("768") int embeddingDims,
        @DefaultValue Es es) {

    /**
     * Elasticsearch 连接配置。
     */
    public record Es(@DefaultValue("localhost") String host, @DefaultValue("9200") int port) {
    }
}
