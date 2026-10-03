package com.zhoubyte.core.config;

import lombok.Data;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.*;

@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
@Data
public class RedisConfig {

    private String host;
    private Integer port;
    private String password;
    private Integer database;
    private Integer connectTimeout;

    @Bean
    public RedisClient redisClient(){
        DefaultJedisClientConfig redisClientConfig = DefaultJedisClientConfig.builder()
                .hostAndPortMapper(hostAndPort -> new HostAndPort(host, port))
                .database(database)
                .connectionTimeoutMillis(connectTimeout)
                .credentials(new DefaultRedisCredentials("", password))
                .build();
        return RedisClient.builder()
                .clientConfig(redisClientConfig)
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
