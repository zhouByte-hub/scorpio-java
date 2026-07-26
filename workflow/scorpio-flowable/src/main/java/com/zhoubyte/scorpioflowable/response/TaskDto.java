package com.zhoubyte.scorpioflowable.response;

import org.flowable.task.api.Task;

import java.util.Date;

/**
 * 任务数据传输对象
 */
public class TaskDto {
    private String id;
    private String name;
    private String description;
    private String assignee;
    private String owner;
    private String parentTaskId;
    private String processInstanceId;
    private String processDefinitionId;
    private String executionId;
    private Date createTime;
    private Date dueDate;
    private String taskDefinitionKey;
    private String state;

    public TaskDto() {
    }

    public TaskDto(Task task) {
        this.id = task.getId();
        this.name = task.getName();
        this.description = task.getDescription();
        this.assignee = task.getAssignee();
        this.owner = task.getOwner();
        this.parentTaskId = task.getParentTaskId();
        this.processInstanceId = task.getProcessInstanceId();
        this.processDefinitionId = task.getProcessDefinitionId();
        this.executionId = task.getExecutionId();
        this.createTime = task.getCreateTime();
        this.dueDate = task.getDueDate();
        this.taskDefinitionKey = task.getTaskDefinitionKey();
        this.state = task.getState();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getParentTaskId() {
        return parentTaskId;
    }

    public void setParentTaskId(String parentTaskId) {
        this.parentTaskId = parentTaskId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public String getTaskDefinitionKey() {
        return taskDefinitionKey;
    }

    public void setTaskDefinitionKey(String taskDefinitionKey) {
        this.taskDefinitionKey = taskDefinitionKey;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}