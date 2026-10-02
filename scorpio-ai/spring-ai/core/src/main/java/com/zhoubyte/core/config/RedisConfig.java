package com.zhoubyte.core.config;

import lombok.Data;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.RedisClient;

@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
@Data
public class RedisConfig {

    private String host;
    private Integer port;

    @Bean
    public RedisClient redisClient(){
        return RedisClient.builder()
                .hostAndPort(host, port)
                .build();
    }


    @Bean
    public RedisVectorStore redisVectorStore(RedisClient redisClient, EmbeddingModel embeddingModel) {
        return RedisVectorStore.builder(redisClient, embeddingModel)
                .prefix("scorpio-")
                .distanceMetric(RedisVectorStore.DistanceMetric.COSINE)
                .build();
    }
}
