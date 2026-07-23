package com.zhoubyte.scorpioflowable.controller;

import com.zhoubyte.scorpioflowable.request.UserLoginDto;
import com.zhoubyte.scorpioflowable.response.Result;
import com.zhoubyte.scorpioflowable.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/start/bpmn")
public class StartBPMNController {

    @Resource
    private UserService userService;

    @PostMapping(value = "/user_login:flowStart")
    public Result<String> startBpmn(@RequestBody UserLoginDto request) {
        String processInstanceId = userService.userLogin(request);
        return Result.success(processInstanceId);
    }



}
