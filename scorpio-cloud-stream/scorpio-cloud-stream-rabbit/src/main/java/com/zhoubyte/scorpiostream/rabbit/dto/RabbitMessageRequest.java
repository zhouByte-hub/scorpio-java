package com.zhoubyte.scorpiostream.rabbit.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record RabbitMessageRequest(
        @NotBlank(message = "消息内容不能为空")
        String content,

        String routingKey,

        Map<String, Object> headers
) {
}
