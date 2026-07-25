package com.zhoubyte.scorpioflowable.controller;

import com.zhoubyte.scorpioflowable.request.UserLoginDto;
import com.zhoubyte.scorpioflowable.response.Result;
import com.zhoubyte.scorpioflowable.service.BPMNService;
import com.zhoubyte.scorpioflowable.service.UserService;
import jakarta.annotation.Resource;
import org.flowable.task.api.Task;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/start/bpmn")
public class StartBPMNController {

    @Resource
    private UserService userService;
    @Resource
    private BPMNService bpmnService;

    /**
     * 启动一个流程
     * @param request 请求参数
     * @return 流程实例ID
     */
    @PostMapping(value = "/user_login:flowStart")
    public Result<String> startBpmn(@RequestBody UserLoginDto request) {
        String processInstanceId = userService.userLogin(request);
        return Result.success(processInstanceId);
    }


    /**
     * 根据流程实例ID获取流程的流程变量
     * @param id 流程实例ID/任务ID
     * @return 流程变量信息
     */
    @GetMapping(value = "/variable/{id}")
    public Result<Map<String, Object>> queryFlowVariable(@PathVariable("id") String id, @RequestParam("type") String type) {
        Map<String, Object> result;
        if("EXECUTION".equals(type)) {
            result = bpmnService.queryVariableFromExecution(id);
        }else {
            result = bpmnService.queryVariableFromTask(id);
        }
        return Result.success(result);
    }

    /**
     * 获取当前用户待处理的任务
     * @return 任务列表
     */
    @GetMapping(value = "/task:active")
    public Result<List<Task>> queryUserActiveTask() {
        return null;
    }


    @GetMapping(value = "/claim")
    public void claimTask(@RequestParam("taskId") String taskId) {
        bpmnService.claimTask(taskId);
    }






}
