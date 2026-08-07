package com.zhoubyte.scorpiostream.rabbit.service;

import com.zhoubyte.scorpiostream.rabbit.dto.MessageSendResponse;
import com.zhoubyte.scorpiostream.rabbit.dto.RabbitBatchMessageRequest;
import com.zhoubyte.scorpiostream.rabbit.dto.RabbitDelayedMessageRequest;
import com.zhoubyte.scorpiostream.rabbit.dto.RabbitMessageRequest;

import java.util.List;

public interface RabbitMessageService {

    MessageSendResponse sendNormal(RabbitMessageRequest request);

    MessageSendResponse sendGroup(RabbitMessageRequest request);

    MessageSendResponse sendDelayed(RabbitDelayedMessageRequest request);

    MessageSendResponse sendDeadLetterDemo(RabbitMessageRequest request);

    List<MessageSendResponse> sendBatch(RabbitBatchMessageRequest request);
}
