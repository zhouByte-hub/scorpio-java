package com.zhoubyte.scorpioflowable.request;

public class UserGroupRequest {

    private String groupName;
    private String type;

    public UserGroupRequest() {
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}