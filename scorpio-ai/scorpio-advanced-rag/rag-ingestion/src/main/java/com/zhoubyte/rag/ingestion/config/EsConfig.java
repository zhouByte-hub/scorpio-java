package com.zhoubyte.rag.ingestion.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Elasticsearch 8.x Java 客户端装配（与 rag-pipeline 同一实例，独立索引）。
 */
@Configuration
public class EsConfig {

    @Bean
    public RestClient elasticsearchRestClient(RagIngestionProperties properties) {
        RagIngestionProperties.Es es = properties.es();
        return RestClient.builder(new HttpHost(es.host(), es.port(), "http")).build();
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(RestClient elasticsearchRestClient) {
        ElasticsearchTransport transport =
                new RestClientTransport(elasticsearchRestClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }
}
