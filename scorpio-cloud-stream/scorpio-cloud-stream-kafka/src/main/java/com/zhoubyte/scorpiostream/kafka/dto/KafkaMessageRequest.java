package com.zhoubyte.scorpiostream.kafka.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record KafkaMessageRequest(
        @NotBlank(message = "消息内容不能为空")
        String content,

        String key,

        Map<String, Object> headers
) {
}
