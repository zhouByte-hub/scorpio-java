package com.zhoubyte.scorpioflowable.service;

import com.zhoubyte.scorpioflowable.request.UserLoginDto;

public interface UserService {

    String userLogin(UserLoginDto userLoginDto);
}
