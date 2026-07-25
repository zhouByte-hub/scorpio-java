package com.zhoubyte.scorpioflowable.utils;

import org.flowable.engine.IdentityService;
import org.flowable.idm.api.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserAuthUtils {

    private final IdentityService identityService;

    public UserAuthUtils(IdentityService identityService) {
        this.identityService = identityService;
    }

    public User currentUser() {
        List<User> list = identityService.createUserQuery().list();
        if(list.isEmpty()) {
            throw new RuntimeException("当前没有用户信息");
        }
        return list.getFirst();
    }
}
