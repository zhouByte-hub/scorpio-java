package com.zhoubyte.scorpioflowable.controller;

import com.zhoubyte.scorpioflowable.request.UserLoginDto;
import com.zhoubyte.scorpioflowable.response.Result;
import com.zhoubyte.scorpioflowable.response.TaskDto;
import com.zhoubyte.scorpioflowable.response.WorkFlowResponse;
import com.zhoubyte.scorpioflowable.service.BPMNService;
import com.zhoubyte.scorpioflowable.service.UserService;
import com.zhoubyte.scorpioflowable.utils.UserAuthUtils;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.task.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/basic_operator")
public class BasicOperatorController {

    private static final Logger log = LoggerFactory.getLogger(BasicOperatorController.class);
    private final UserAuthUtils userAuthUtils;
    private final UserService userService;
    private final BPMNService bpmnService;
    private final TaskService taskService;
    private final RepositoryService repositoryService;

    /**
     * 构造函数注入依赖
     */
    public BasicOperatorController(UserAuthUtils userAuthUtils,
                                    UserService userService,
                                    BPMNService bpmnService,
                                    TaskService taskService,
                                    RepositoryService repositoryService) {
        this.userAuthUtils = userAuthUtils;
        this.userService = userService;
        this.bpmnService = bpmnService;
        this.taskService = taskService;
        this.repositoryService = repositoryService;
    }


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
     * 任务拾取
     * @param taskId 任务ID
     */
    @GetMapping(value = "/claim")
    public Result<String> claimTask(@RequestParam("taskId") String taskId) {
        bpmnService.claimTask(taskId);
        return Result.success(taskId);
    }

    /**
     * 任务归还
     * @param taskId 任务ID
     */
    @GetMapping(value = "/unclaim")
    public Result<String> unclaimTask(String taskId) {
        bpmnService.unclaimTask(taskId);
        return Result.success(taskId);
    }


    /**
     * 获取指定用户的待办任务
     * @return 用户待办任务列表
     */
    @GetMapping(value = "/assignee")
    public Result<List<TaskDto>> queryAssignee() {
        List<Task> tasks = taskService.createTaskQuery()
                .taskAssignee(userAuthUtils.currentUser().getId())
                .list();
        List<TaskDto> taskDtos = tasks.stream()
                .map(TaskDto::new)
                .collect(Collectors.toList());
        return Result.success(taskDtos);
    }


    /**
     * 将流程定义挂起，不能再进行流程的实例化
     * @param processDefinitionId 流程定义ID
     * @return 操作结果
     */
    @GetMapping(value = "/suspended_activity/{processDefinitionId}")
    public Result<String> suspendedActivity(@PathVariable("processDefinitionId") String processDefinitionId) {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId)
                .singleResult();

        if(processDefinition == null) {
            log.error("{} 不存在", processDefinitionId);
            return Result.error(String.format("%s不存在", processDefinitionId));
        }
        if(processDefinition.isSuspended()) {
            repositoryService.activateProcessDefinitionById(processDefinitionId);
            return Result.success(String.format("%s已激活", processDefinitionId));
        }else {
            repositoryService.suspendProcessDefinitionById(processDefinitionId);
            return Result.success(String.format("%s已挂起", processDefinitionId));
        }
    }


    @PostMapping(value = "/complete")
    public Result<String> completeUserTask(@RequestParam("taskId") String taskId, @RequestBody Map<String, Object> params) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if(task == null) {
            return Result.error(String.format("%s任务不存在", taskId));
        }
        String state = task.getState();
        taskService.complete(task.getId(), params);
        return Result.success(state);
    }

    @GetMapping(value = "/current_user_active_workflow")
    public Result<List<WorkFlowResponse>> queryCurrentUserActiveWorkflow() {
        List<WorkFlowResponse> workFlowResponses = bpmnService.currentActiveWorkflow();
        return Result.success(workFlowResponses);
    }
}
