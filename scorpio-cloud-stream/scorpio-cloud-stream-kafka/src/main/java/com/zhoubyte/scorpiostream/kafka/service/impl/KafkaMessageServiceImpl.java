package com.zhoubyte.scorpiostream.kafka.service.impl;

import com.zhoubyte.scorpiostream.kafka.dto.KafkaBatchMessageRequest;
import com.zhoubyte.scorpiostream.kafka.dto.KafkaMessageRequest;
import com.zhoubyte.scorpiostream.kafka.dto.MessageSendResponse;
import com.zhoubyte.scorpiostream.kafka.service.KafkaMessageService;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Service
public class KafkaMessageServiceImpl implements KafkaMessageService {

    private static final String NORMAL_BINDING = "kafkaNormal-out-0";
    private static final String GROUP_BINDING = "kafkaGroup-out-0";
    private static final String PARTITION_BINDING = "kafkaPartition-out-0";
    private static final String ERROR_BINDING = "kafkaError-out-0";
    private static final String PARTITION_KEY_HEADER = "partitionKey";

    private final StreamBridge streamBridge;

    public KafkaMessageServiceImpl(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @Override
    public MessageSendResponse sendNormal(KafkaMessageRequest request) {
        return send(NORMAL_BINDING, request.content(), request.key(), request.headers());
    }

    @Override
    public MessageSendResponse sendGroup(KafkaMessageRequest request) {
        return send(GROUP_BINDING, request.content(), request.key(), request.headers());
    }

    @Override
    public MessageSendResponse sendPartition(KafkaMessageRequest request) {
        return send(PARTITION_BINDING, request.content(), request.key(), request.headers());
    }

    @Override
    public MessageSendResponse sendErrorDemo(KafkaMessageRequest request) {
        return send(ERROR_BINDING, request.content(), request.key(), request.headers());
    }

    @Override
    public List<MessageSendResponse> sendBatch(KafkaBatchMessageRequest request) {
        return request.messages().stream().map(this::sendNormal).toList();
    }

    private MessageSendResponse send(String bindingName, String content, String key, Map<String, Object> headers) {
        MessageBuilder<String> builder = MessageBuilder.withPayload(content);
        if (!CollectionUtils.isEmpty(headers)) {
            headers.forEach(builder::setHeader);
        }
        if (StringUtils.hasText(key)) {
            builder.setHeader(PARTITION_KEY_HEADER, key);
        }
        Message<String> message = builder.build();
        boolean sent = streamBridge.send(bindingName, message);
        return new MessageSendResponse(bindingName, content, sent);
    }
}
