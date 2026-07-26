package com.zhoubyte.scorpioflowable.response;

import java.util.List;
import java.util.Map;

public class WorkFlowResponse {

    private String processId;
    private String executionId;
    private String processName;
    private Integer version;
    private String businessKey;
    private String startTime;
    private String status;
    private Map<String, Object> params;
    private List<TaskResponse> taskResponseList;

    private WorkFlowResponse() {
    }

    private WorkFlowResponse(String processId, String executionId, String processName,
                            Integer version, String businessKey, String startTime,
                            String status, Map<String, Object> params,
                            List<TaskResponse> taskResponseList) {
        this.processId = processId;
        this.executionId = executionId;
        this.processName = processName;
        this.version = version;
        this.businessKey = businessKey;
        this.startTime = startTime;
        this.status = status;
        this.params = params;
        this.taskResponseList = taskResponseList;
    }

    public static WorkFlowResponseBuilder builder() {
        return new WorkFlowResponseBuilder();
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public List<TaskResponse> getTaskResponseList() {
        return taskResponseList;
    }

    public void setTaskResponseList(List<TaskResponse> taskResponseList) {
        this.taskResponseList = taskResponseList;
    }

    public static class WorkFlowResponseBuilder {
        private String processId;
        private String executionId;
        private String processName;
        private Integer version;
        private String businessKey;
        private String startTime;
        private String status;
        private Map<String, Object> params;
        private List<TaskResponse> taskResponseList;

        WorkFlowResponseBuilder() {
        }

        public WorkFlowResponseBuilder processId(String processId) {
            this.processId = processId;
            return this;
        }

        public WorkFlowResponseBuilder executionId(String executionId) {
            this.executionId = executionId;
            return this;
        }

        public WorkFlowResponseBuilder processName(String processName) {
            this.processName = processName;
            return this;
        }

        public WorkFlowResponseBuilder version(Integer version) {
            this.version = version;
            return this;
        }

        public WorkFlowResponseBuilder businessKey(String businessKey) {
            this.businessKey = businessKey;
            return this;
        }

        public WorkFlowResponseBuilder startTime(String startTime) {
            this.startTime = startTime;
            return this;
        }

        public WorkFlowResponseBuilder status(String status) {
            this.status = status;
            return this;
        }

        public WorkFlowResponseBuilder params(Map<String, Object> params) {
            this.params = params;
            return this;
        }

        public WorkFlowResponseBuilder taskResponseList(List<TaskResponse> taskResponseList) {
            this.taskResponseList = taskResponseList;
            return this;
        }

        public WorkFlowResponse build() {
            return new WorkFlowResponse(processId, executionId, processName,
                    version, businessKey, startTime, status, params, taskResponseList);
        }
    }

    @Override
    public String toString() {
        return "WorkFlowResponse{" +
                "processId='" + processId + '\'' +
                ", executionId='" + executionId + '\'' +
                ", processName='" + processName + '\'' +
                ", version=" + version +
                ", businessKey='" + businessKey + '\'' +
                ", startTime='" + startTime + '\'' +
                ", status='" + status + '\'' +
                ", params=" + params +
                ", taskResponseList=" + taskResponseList +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WorkFlowResponse that = (WorkFlowResponse) o;

        if (processId != null ? !processId.equals(that.processId) : that.processId != null) return false;
        if (executionId != null ? !executionId.equals(that.executionId) : that.executionId != null) return false;
        if (processName != null ? !processName.equals(that.processName) : that.processName != null) return false;
        if (version != null ? !version.equals(that.version) : that.version != null) return false;
        if (businessKey != null ? !businessKey.equals(that.businessKey) : that.businessKey != null) return false;
        if (startTime != null ? !startTime.equals(that.startTime) : that.startTime != null) return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;
        if (params != null ? !params.equals(that.params) : that.params != null) return false;
        return taskResponseList != null ? taskResponseList.equals(that.taskResponseList) : that.taskResponseList == null;
    }

    @Override
    public int hashCode() {
        int result = processId != null ? processId.hashCode() : 0;
        result = 31 * result + (executionId != null ? executionId.hashCode() : 0);
        result = 31 * result + (processName != null ? processName.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (businessKey != null ? businessKey.hashCode() : 0);
        result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (params != null ? params.hashCode() : 0);
        result = 31 * result + (taskResponseList != null ? taskResponseList.hashCode() : 0);
        return result;
    }

    public static class TaskResponse {

        private String taskId;
        private String status;
        private String name;
        private String assignee;
        private String createTime;
        private String claimTime;
        private Map<String, Object> params;

        private TaskResponse() {
        }

        private TaskResponse(String taskId, String status, String name,
                            String assignee, String createTime, String claimTime,
                            Map<String, Object> params) {
            this.taskId = taskId;
            this.status = status;
            this.name = name;
            this.assignee = assignee;
            this.createTime = createTime;
            this.claimTime = claimTime;
            this.params = params;
        }

        public static TaskResponseBuilder builder() {
            return new TaskResponseBuilder();
        }

        public String getTaskId() {
            return taskId;
        }

        public void setTaskId(String taskId) {
            this.taskId = taskId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAssignee() {
            return assignee;
        }

        public void setAssignee(String assignee) {
            this.assignee = assignee;
        }

        public String getCreateTime() {
            return createTime;
        }

        public void setCreateTime(String createTime) {
            this.createTime = createTime;
        }

        public String getClaimTime() {
            return claimTime;
        }

        public void setClaimTime(String claimTime) {
            this.claimTime = claimTime;
        }

        public Map<String, Object> getParams() {
            return params;
        }

        public void setParams(Map<String, Object> params) {
            this.params = params;
        }

        @Override
        public String toString() {
            return "TaskResponse{" +
                    "taskId='" + taskId + '\'' +
                    ", status='" + status + '\'' +
                    ", name='" + name + '\'' +
                    ", assignee='" + assignee + '\'' +
                    ", createTime='" + createTime + '\'' +
                    ", claimTime='" + claimTime + '\'' +
                    ", params=" + params +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TaskResponse that = (TaskResponse) o;

            if (taskId != null ? !taskId.equals(that.taskId) : that.taskId != null) return false;
            if (status != null ? !status.equals(that.status) : that.status != null) return false;
            if (name != null ? !name.equals(that.name) : that.name != null) return false;
            if (assignee != null ? !assignee.equals(that.assignee) : that.assignee != null) return false;
            if (createTime != null ? !createTime.equals(that.createTime) : that.createTime != null) return false;
            if (claimTime != null ? !claimTime.equals(that.claimTime) : that.claimTime != null) return false;
            return params != null ? params.equals(that.params) : that.params == null;
        }

        @Override
        public int hashCode() {
            int result = taskId != null ? taskId.hashCode() : 0;
            result = 31 * result + (status != null ? status.hashCode() : 0);
            result = 31 * result + (name != null ? name.hashCode() : 0);
            result = 31 * result + (assignee != null ? assignee.hashCode() : 0);
            result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
            result = 31 * result + (claimTime != null ? claimTime.hashCode() : 0);
            result = 31 * result + (params != null ? params.hashCode() : 0);
            return result;
        }

        public static class TaskResponseBuilder {
            private String taskId;
            private String status;
            private String name;
            private String assignee;
            private String createTime;
            private String claimTime;
            private Map<String, Object> params;

            TaskResponseBuilder() {
            }

            public TaskResponseBuilder taskId(String taskId) {
                this.taskId = taskId;
                return this;
            }

            public TaskResponseBuilder status(String status) {
                this.status = status;
                return this;
            }

            public TaskResponseBuilder name(String name) {
                this.name = name;
                return this;
            }

            public TaskResponseBuilder assignee(String assignee) {
                this.assignee = assignee;
                return this;
            }

            public TaskResponseBuilder createTime(String createTime) {
                this.createTime = createTime;
                return this;
            }

            public TaskResponseBuilder claimTime(String claimTime) {
                this.claimTime = claimTime;
                return this;
            }

            public TaskResponseBuilder params(Map<String, Object> params) {
                this.params = params;
                return this;
            }

            public TaskResponse build() {
                return new TaskResponse(taskId, status, name, assignee, createTime, claimTime, params);
            }
        }
    }
}