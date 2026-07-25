package com.zhoubyte.scorpioflowable.service.impl;

import com.zhoubyte.scorpioflowable.service.BPMNService;
import com.zhoubyte.scorpioflowable.utils.UserAuthUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.Execution;
import org.flowable.idm.api.User;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class BPMNServiceImpl implements BPMNService {

    private final TaskService taskService;
    private final RuntimeService runtimeService;
    private final UserAuthUtils userAuthUtils;

    /**
     * 根据执行实例ID查询流程变量
     *
     * 说明：
     * 1. 执行实例(Execution)代表流程执行路径中的一个节点，流程实例(ProcessInstance)也是一种特殊的执行实例
     * 2. 该方法查询的是流程变量(Variable)，作用域为整个流程实例，所有任务和执行实例都可以访问
     * 3. 流程变量存储在ACT_RU_VARIABLE表中，通过PROC_INST_ID_关联流程实例
     * 4. 与局部变量(Variable Local)的区别：
     *    - 流程变量：作用域为整个流程实例，所有节点共享
     *    - 局部变量：作用域为特定的执行实例或任务，仅当前节点可访问
     *
     * @param executionId 执行实例ID，可以是流程实例ID或子执行实例ID
     * @return 流程变量Map，key为变量名，value为变量值。如果executionId为空返回空Map
     * @throws RuntimeException 如果执行实例不存在
     */
    @Override
    public Map<String, Object> queryVariableFromExecution(String executionId) {
        if(StringUtils.isEmpty(executionId)) {
            return Map.of();
        }
        Execution execution = runtimeService.createExecutionQuery().executionId(executionId).singleResult();
        if(execution == null) {
            throw new RuntimeException("Execution not exits");
        }
        return runtimeService.getVariables(executionId);
    }

    @Override
    public Map<String, Object> queryVariableFromTask(String taskId) {
        if(StringUtils.isEmpty(taskId)) {
            return Map.of();
        }
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if(task == null) {
            throw new RuntimeException("Task not exits");
        }
        return taskService.getVariables(taskId);
    }

    @Override
    public void claimTask(String taskId) {
        if(StringUtils.isEmpty(taskId)) {
            throw new RuntimeException("任务编号不能为空");
        }
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if(task == null) {
            throw new RuntimeException("任务不存在");
        }
        User user = userAuthUtils.currentUser();
        if(user == null) {
            throw new RuntimeException("请登陆");
        }
        taskService.claim(task.getId(), user.getId());
    }

    @Override
    public void unclaimTask(String taskId) {
        taskService.unclaim(taskId);
    }


}
