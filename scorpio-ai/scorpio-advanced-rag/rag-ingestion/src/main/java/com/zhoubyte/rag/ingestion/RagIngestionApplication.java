package com.zhoubyte.rag.ingestion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * rag-ingestion 启动类：多格式文档解析 + 结构感知分块 + ES 独立索引。
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class RagIngestionApplication {

    public static void main(String[] args) {
        SpringApplication.run(RagIngestionApplication.class, args);
    }
}
