package com.zhoubyte.scorpiostream.rabbit.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

@Configuration
public class RabbitConsumerConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitConsumerConfig.class);

    @Bean
    public Consumer<Message<String>> rabbitNormalConsumer() {
        return message -> LOGGER.info("Rabbit normal message: {}", message.getPayload());
    }

    @Bean
    public Consumer<Message<String>> rabbitGroupConsumer() {
        return message -> LOGGER.info("Rabbit group message: {}", message.getPayload());
    }

    @Bean
    public Consumer<Message<String>> rabbitErrorConsumer() {
        return message -> {
            LOGGER.info("Rabbit error demo message: {}", message.getPayload());
            throw new IllegalStateException("Rabbit dead letter demo");
        };
    }
}
