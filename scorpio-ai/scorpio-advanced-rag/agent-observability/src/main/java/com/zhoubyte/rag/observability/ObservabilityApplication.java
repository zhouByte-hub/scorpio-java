package com.zhoubyte.rag.observability;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Agent 可观测示例启动类。
 *
 * <p>链路：Spring AI 内置 Micrometer Observation 埋点（ChatClient/ChatModel/Tool 自动产出
 * gen_ai.* 语义约定 Span）→ OTLP 导出到 OTel Collector（统一接收、过滤、采样）→
 * Langfuse（Trace 存储、可视化、评测、人工标注、成本看板）。</p>
 */
@SpringBootApplication
public class ObservabilityApplication {

    public static void main(String[] args) {
        SpringApplication.run(ObservabilityApplication.class, args);
    }
}
