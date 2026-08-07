package com.zhoubyte.scorpiostream.kafka.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record KafkaBatchMessageRequest(
        @Valid
        @NotEmpty(message = "消息列表不能为空")
        List<KafkaMessageRequest> messages
) {
}
