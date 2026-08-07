package com.zhoubyte.scorpiostream.rabbit.service.impl;

import com.zhoubyte.scorpiostream.rabbit.dto.MessageSendResponse;
import com.zhoubyte.scorpiostream.rabbit.dto.RabbitBatchMessageRequest;
import com.zhoubyte.scorpiostream.rabbit.dto.RabbitDelayedMessageRequest;
import com.zhoubyte.scorpiostream.rabbit.dto.RabbitMessageRequest;
import com.zhoubyte.scorpiostream.rabbit.service.RabbitMessageService;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Service
public class RabbitMessageServiceImpl implements RabbitMessageService {

    private static final String NORMAL_BINDING = "rabbitNormal-out-0";
    private static final String GROUP_BINDING = "rabbitGroup-out-0";
    private static final String DELAYED_BINDING = "rabbitDelayed-out-0";
    private static final String ERROR_BINDING = "rabbitError-out-0";
    private static final String ROUTING_KEY_HEADER = "routingKey";

    private final StreamBridge streamBridge;

    public RabbitMessageServiceImpl(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @Override
    public MessageSendResponse sendNormal(RabbitMessageRequest request) {
        return send(NORMAL_BINDING, request.content(), request.routingKey(), request.headers(), null);
    }

    @Override
    public MessageSendResponse sendGroup(RabbitMessageRequest request) {
        return send(GROUP_BINDING, request.content(), request.routingKey(), request.headers(), null);
    }

    @Override
    public MessageSendResponse sendDelayed(RabbitDelayedMessageRequest request) {
        return send(
                DELAYED_BINDING,
                request.content(),
                request.routingKey(),
                request.headers(),
                request.actualDelayMillis()
        );
    }

    @Override
    public MessageSendResponse sendDeadLetterDemo(RabbitMessageRequest request) {
        return send(ERROR_BINDING, request.content(), request.routingKey(), request.headers(), null);
    }

    @Override
    public List<MessageSendResponse> sendBatch(RabbitBatchMessageRequest request) {
        return request.messages().stream().map(this::sendNormal).toList();
    }

    private MessageSendResponse send(
            String bindingName,
            String content,
            String routingKey,
            Map<String, Object> headers,
            Integer delayMillis) {
        MessageBuilder<String> builder = MessageBuilder.withPayload(content);
        if (!CollectionUtils.isEmpty(headers)) {
            headers.forEach(builder::setHeader);
        }
        if (StringUtils.hasText(routingKey)) {
            builder.setHeader(ROUTING_KEY_HEADER, routingKey);
        }
        if (delayMillis != null) {
            builder.setHeader(AmqpHeaders.DELAY, delayMillis);
        }
        Message<String> message = builder.build();
        boolean sent = streamBridge.send(bindingName, message);
        return new MessageSendResponse(bindingName, content, sent);
    }
}
