package com.zhoubyte.scorpioflowable.controller;

import com.zhoubyte.scorpioflowable.entity.UserEntity;
import com.zhoubyte.scorpioflowable.request.UserGroupMembershipRequest;
import com.zhoubyte.scorpioflowable.request.UserGroupRequest;
import com.zhoubyte.scorpioflowable.response.Result;
import org.flowable.engine.IdentityService;
import org.flowable.idm.api.Group;
import org.flowable.idm.api.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(value = "/identity")
public class BPMNUserController {

    private static final Logger log = LoggerFactory.getLogger(BPMNUserController.class);
    private final IdentityService identityService;

    public BPMNUserController(IdentityService identityService) {
        this.identityService = identityService;
    }


    @PostMapping(value = "/user:create")
    public Result<User> createUser(@RequestBody UserEntity userEntity){
        User user = identityService.newUser(UUID.randomUUID().toString().replace("-", ""));
        user.setDisplayName(userEntity.getUsername());
        user.setEmail(userEntity.getEmail());
        identityService.saveUser(user);
        return Result.success(user);
    }

    @PostMapping(value = "/group:create")
    public Result<Group> createGroup(@RequestBody UserGroupRequest userGroupRequest) {
        Group group = identityService.newGroup(UUID.randomUUID().toString().replace("-", ""));
        group.setName(userGroupRequest.getGroupName());
        group.setType(userGroupRequest.getType());
        identityService.saveGroup(group);
        return Result.success(group);
    }


    @PostMapping(value = "/membership:create")
    public Result<Map<String, Integer>> createMembership(@RequestBody UserGroupMembershipRequest request) {
        String groupId = request.getGroupId();
        Group group = identityService.createGroupQuery().groupId(groupId).singleResult();
        if(group == null) {
            throw new RuntimeException("对应组织不存在");
        }
        Map<String, Integer> count = new HashMap<>();
        count.put("groupCount", 1);
        for (String userId : request.getUserIds()) {
            User user = identityService.createUserQuery().userId(userId).singleResult();
            if(user == null) {
                log.info("{}用户不存在", userId);
                continue;
            }
            identityService.createMembership(userId, group.getId());
            count.put("userCount", count.getOrDefault("userCount", 0) + 1);
        }
        return Result.success(count);
    }


}
