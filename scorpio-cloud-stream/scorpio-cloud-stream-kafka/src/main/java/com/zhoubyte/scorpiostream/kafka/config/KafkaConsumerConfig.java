package com.zhoubyte.scorpiostream.kafka.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

@Configuration
public class KafkaConsumerConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    @Bean
    public Consumer<Message<String>> kafkaNormalConsumer() {
        return message -> LOGGER.info("Kafka normal message: {}", message.getPayload());
    }

    @Bean
    public Consumer<Message<String>> kafkaGroupConsumer() {
        return message -> LOGGER.info("Kafka group message: {}", message.getPayload());
    }

    @Bean
    public Consumer<Message<String>> kafkaErrorConsumer() {
        return message -> {
            LOGGER.info("Kafka error demo message: {}", message.getPayload());
            throw new IllegalStateException("Kafka DLQ demo");
        };
    }
}
