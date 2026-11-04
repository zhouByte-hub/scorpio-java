package com.zhoubyte.rag.pipeline.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * RAG 流水线可调参数，见 application.yaml 中 scorpio.rag.* 配置。
 *
 * @param channelTopK     每路召回通道取回的候选数
 * @param rrfK            RRF 倒数排名融合常数，越大排名差异越平滑
 * @param rerankTopN      重排后保留的上下文 chunk 数
 * @param rerankThreshold 重排相关性得分（0~10）低于该阈值的候选直接剔除
 * @param denseEnabled    是否启用稠密向量召回通道（口语化问句多时建议开启）
 * @param verifyEnabled   是否启用 LLM 事实校验层
 * @param indexName       Elasticsearch 索引名
 * @param embeddingDims   稠密向量维度（nomic-embed-text 为 768）
 * @param es              Elasticsearch 连接配置
 */
@ConfigurationProperties(prefix = "scorpio.rag")
public record RagProperties(
        @DefaultValue("20") int channelTopK,
        @DefaultValue("60") int rrfK,
        @DefaultValue("5") int rerankTopN,
        @DefaultValue("5.0") double rerankThreshold,
        @DefaultValue("true") boolean denseEnabled,
        @DefaultValue("true") boolean verifyEnabled,
        @DefaultValue("rag_chunks") String indexName,
        @DefaultValue("768") int embeddingDims,
        @DefaultValue Es es) {

    /**
     * Elasticsearch 连接配置。
     */
    public record Es(@DefaultValue("localhost") String host, @DefaultValue("9200") int port) {
    }
}
