package com.zhoubyte.rag.pipeline;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * 企业级高准确率 RAG 流水线启动类。
 *
 * <p>完整链路：Query 前置处理（清洗/意图分类/RoaringBitmap 元数据过滤）→ 多 Query 改写 →
 * BM25 / 稀疏 / 稠密 多路并行召回 → RRF 倒数排名融合 → LLM 重排 → 上下文组装与溯源锚定 →
 * LLM 事实校验 → 带引用来源的答案。</p>
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class RagPipelineApplication {

    public static void main(String[] args) {
        SpringApplication.run(RagPipelineApplication.class, args);
    }
}
