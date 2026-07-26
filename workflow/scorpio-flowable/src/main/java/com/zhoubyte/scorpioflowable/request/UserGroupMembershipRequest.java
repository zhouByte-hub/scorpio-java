package com.zhoubyte.scorpioflowable.request;

import java.util.List;

public class UserGroupMembershipRequest {

    private String groupId;
    private List<String> userIds;

    public UserGroupMembershipRequest() {
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }
}