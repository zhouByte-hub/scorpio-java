package com.zhoubyte.scorpiostream.rabbit.dto;

public record MessageSendResponse(
        String bindingName,
        String content,
        Boolean sent
) {
}
