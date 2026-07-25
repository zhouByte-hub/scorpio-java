package com.zhoubyte.scorpioflowable.request;

import lombok.Data;

import java.util.List;

@Data
public class UserGroupMembershipRequest {

    private String groupId;
    private List<String> userIds;
}
