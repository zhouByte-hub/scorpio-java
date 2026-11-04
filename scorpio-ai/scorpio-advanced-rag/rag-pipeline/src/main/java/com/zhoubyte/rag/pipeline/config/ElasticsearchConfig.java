package com.zhoubyte.rag.pipeline.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Elasticsearch 8.x Java 客户端装配。
 *
 * <p>使用 elasticsearch-java（Elasticsearch API Client）+ RestClient 传输层：
 * <ul>
 *   <li>BM25 召回：原生 match 全文检索；</li>
 *   <li>稀疏召回：rank_features 字段加权词项检索；</li>
 *   <li>稠密召回：dense_vector + knn 向量检索。</li>
 * </ul>
 * 序列化复用 Spring 容器内的 Jackson ObjectMapper。
 * </p>
 */
@Configuration
public class ElasticsearchConfig {

    @Bean
    public RestClient elasticsearchRestClient(RagProperties properties) {
        RagProperties.Es es = properties.es();
        return RestClient.builder(new HttpHost(es.host(), es.port(), "http")).build();
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(RestClient elasticsearchRestClient) {
        ElasticsearchTransport transport =
                new RestClientTransport(elasticsearchRestClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }
}
