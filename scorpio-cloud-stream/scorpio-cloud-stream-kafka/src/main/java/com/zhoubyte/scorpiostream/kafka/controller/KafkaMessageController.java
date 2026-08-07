package com.zhoubyte.scorpiostream.kafka.controller;

import com.zhoubyte.scorpiostream.kafka.common.Result;
import com.zhoubyte.scorpiostream.kafka.dto.KafkaBatchMessageRequest;
import com.zhoubyte.scorpiostream.kafka.dto.KafkaMessageRequest;
import com.zhoubyte.scorpiostream.kafka.dto.MessageSendResponse;
import com.zhoubyte.scorpiostream.kafka.service.KafkaMessageService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/kafka/messages")
public class KafkaMessageController {

    private final KafkaMessageService kafkaMessageService;

    public KafkaMessageController(KafkaMessageService kafkaMessageService) {
        this.kafkaMessageService = kafkaMessageService;
    }

    @PostMapping("/normal")
    public Result<MessageSendResponse> sendNormal(@Valid @RequestBody KafkaMessageRequest request) {
        return Result.success(kafkaMessageService.sendNormal(request));
    }

    @PostMapping("/group")
    public Result<MessageSendResponse> sendGroup(@Valid @RequestBody KafkaMessageRequest request) {
        return Result.success(kafkaMessageService.sendGroup(request));
    }

    @PostMapping("/partition")
    public Result<MessageSendResponse> sendPartition(@Valid @RequestBody KafkaMessageRequest request) {
        return Result.success(kafkaMessageService.sendPartition(request));
    }

    @PostMapping("/error")
    public Result<MessageSendResponse> sendErrorDemo(@Valid @RequestBody KafkaMessageRequest request) {
        return Result.success(kafkaMessageService.sendErrorDemo(request));
    }

    @PostMapping("/batch")
    public Result<List<MessageSendResponse>> sendBatch(@Valid @RequestBody KafkaBatchMessageRequest request) {
        return Result.success(kafkaMessageService.sendBatch(request));
    }
}
