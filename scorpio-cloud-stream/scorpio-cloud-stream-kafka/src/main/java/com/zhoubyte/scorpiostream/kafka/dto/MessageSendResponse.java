package com.zhoubyte.scorpiostream.kafka.dto;

public record MessageSendResponse(
        String bindingName,
        String content,
        Boolean sent
) {
}
