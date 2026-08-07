package com.zhoubyte.scorpiostream.rabbit.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record RabbitBatchMessageRequest(
        @Valid
        @NotEmpty(message = "消息列表不能为空")
        List<RabbitMessageRequest> messages
) {
}
