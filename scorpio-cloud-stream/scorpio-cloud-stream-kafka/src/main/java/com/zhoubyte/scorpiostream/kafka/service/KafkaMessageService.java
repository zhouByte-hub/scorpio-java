package com.zhoubyte.scorpiostream.kafka.service;

import com.zhoubyte.scorpiostream.kafka.dto.KafkaBatchMessageRequest;
import com.zhoubyte.scorpiostream.kafka.dto.KafkaMessageRequest;
import com.zhoubyte.scorpiostream.kafka.dto.MessageSendResponse;

import java.util.List;

public interface KafkaMessageService {

    MessageSendResponse sendNormal(KafkaMessageRequest request);

    MessageSendResponse sendGroup(KafkaMessageRequest request);

    MessageSendResponse sendPartition(KafkaMessageRequest request);

    MessageSendResponse sendErrorDemo(KafkaMessageRequest request);

    List<MessageSendResponse> sendBatch(KafkaBatchMessageRequest request);
}
