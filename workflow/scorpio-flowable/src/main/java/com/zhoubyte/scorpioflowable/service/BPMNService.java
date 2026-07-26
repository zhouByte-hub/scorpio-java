package com.zhoubyte.scorpioflowable.service;

import com.zhoubyte.scorpioflowable.response.WorkFlowResponse;

import java.util.List;
import java.util.Map;

public interface BPMNService {

    Map<String, Object> queryVariableFromExecution(String executionId);

    Map<String, Object> queryVariableFromTask(String taskId);

    void claimTask(String taskId);

    void unclaimTask(String taskId);

    List<WorkFlowResponse> currentActiveWorkflow();
}
