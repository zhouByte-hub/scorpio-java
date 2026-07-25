package com.zhoubyte.scorpioflowable.controller;

import ch.qos.logback.core.util.StatusPrinter2;
import com.zhoubyte.scorpioflowable.response.Result;
import com.zhoubyte.scorpioflowable.utils.UserAuthUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.task.api.Task;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(value = "/basic_operator")
@RequiredArgsConstructor
public class BasicOperatorController {

    private final TaskService taskService;
    private final RepositoryService repositoryService;
    private final RuntimeService runtimeService;
    private final UserAuthUtils  userAuthUtils;

    // 获取指定用户的任务
    @GetMapping(value = "/assignee")
    public Result<List<Task>> queryAssignee() {
        List<Task> list = taskService.createTaskQuery()
                .taskAssignee(userAuthUtils.currentUser().getId())
                .active()
                .list();
        return Result.success(list);
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
}
