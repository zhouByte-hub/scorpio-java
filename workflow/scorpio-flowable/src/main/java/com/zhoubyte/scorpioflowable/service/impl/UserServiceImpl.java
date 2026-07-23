package com.zhoubyte.scorpioflowable.service.impl;

import com.zhoubyte.scorpioflowable.entity.UserEntity;
import com.zhoubyte.scorpioflowable.request.UserLoginDto;
import com.zhoubyte.scorpioflowable.service.UserService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.common.engine.api.FlowableException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    private final static String USER_LOGIN_FLOW_KEY = "USER_LOGIN";
    private final static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private RuntimeService runtimeService;

    @Override
    public String userLogin(UserLoginDto userLoginDto) {
        // 1. 参数校验
        if(StringUtils.isEmpty(userLoginDto.getUsername())) {
            throw new RuntimeException("登陆用户名不能为空");
        }
        if(StringUtils.isEmpty(userLoginDto.getPassword())) {
            throw new RuntimeException("登陆密码不能为空");
        }
        // 2. 模拟用户数据（demo 环境）
        UserEntity userEntity = UserEntity.of(userLoginDto.getUsername());

        // 3. 构建流程变量
        Map<String, Object> variables = new HashMap<>();
        variables.put("userInfo", userEntity);
        variables.put("startTime", LocalDateTime.now().format(DATE_FORMATTER));

        // 4. 启动登录流程
        try {
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(USER_LOGIN_FLOW_KEY, variables);
            return processInstance.getId();
        } catch (FlowableException e) {
            throw new RuntimeException("登陆流程启动失败: " + e.getMessage(), e);
        }
    }
}
