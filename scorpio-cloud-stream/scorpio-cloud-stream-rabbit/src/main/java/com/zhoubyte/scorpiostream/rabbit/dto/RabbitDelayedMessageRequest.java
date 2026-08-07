package com.zhoubyte.scorpiostream.rabbit.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record RabbitDelayedMessageRequest(
        @NotBlank(message = "消息内容不能为空")
        String content,

        @Min(value = 1, message = "延迟时间必须大于 0")
        Integer delayMillis,

        String routingKey,

        Map<String, Object> headers
) {
    public int actualDelayMillis() {
        return delayMillis == null ? 5000 : delayMillis;
    }
}
