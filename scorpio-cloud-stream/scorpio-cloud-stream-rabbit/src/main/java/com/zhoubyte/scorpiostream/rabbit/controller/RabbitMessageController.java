package com.zhoubyte.scorpiostream.rabbit.controller;

import com.zhoubyte.scorpiostream.rabbit.common.Result;
import com.zhoubyte.scorpiostream.rabbit.dto.MessageSendResponse;
import com.zhoubyte.scorpiostream.rabbit.dto.RabbitBatchMessageRequest;
import com.zhoubyte.scorpiostream.rabbit.dto.RabbitDelayedMessageRequest;
import com.zhoubyte.scorpiostream.rabbit.dto.RabbitMessageRequest;
import com.zhoubyte.scorpiostream.rabbit.service.RabbitMessageService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rabbit/messages")
public class RabbitMessageController {

    private final RabbitMessageService rabbitMessageService;

    public RabbitMessageController(RabbitMessageService rabbitMessageService) {
        this.rabbitMessageService = rabbitMessageService;
    }

    @PostMapping("/normal")
    public Result<MessageSendResponse> sendNormal(@Valid @RequestBody RabbitMessageRequest request) {
        return Result.success(rabbitMessageService.sendNormal(request));
    }

    @PostMapping("/group")
    public Result<MessageSendResponse> sendGroup(@Valid @RequestBody RabbitMessageRequest request) {
        return Result.success(rabbitMessageService.sendGroup(request));
    }

    @PostMapping("/delayed")
    public Result<MessageSendResponse> sendDelayed(@Valid @RequestBody RabbitDelayedMessageRequest request) {
        return Result.success(rabbitMessageService.sendDelayed(request));
    }

    @PostMapping("/dead-letter")
    public Result<MessageSendResponse> sendDeadLetterDemo(@Valid @RequestBody RabbitMessageRequest request) {
        return Result.success(rabbitMessageService.sendDeadLetterDemo(request));
    }

    @PostMapping("/batch")
    public Result<List<MessageSendResponse>> sendBatch(@Valid @RequestBody RabbitBatchMessageRequest request) {
        return Result.success(rabbitMessageService.sendBatch(request));
    }
}
