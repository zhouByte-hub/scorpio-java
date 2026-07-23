package com.zhoubyte.scorpioflowable.entity;

import com.zhoubyte.scorpioflowable.utils.AccountStatusEnum;
import com.zhoubyte.scorpioflowable.utils.SexEnum;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserEntity implements Serializable {

    private String id;
    private String username;
    private String nickname;
    private String email;
    private String province;
    private String city;
    private SexEnum sex;
    private String work;
    private String phone;
    private AccountStatusEnum status;
    private LocalDateTime lastLoginTime;
    private Boolean locked;


    public static UserEntity of(String username){
        if(StringUtils.isEmpty(username)) {
            return null;
        }
        UserEntity userEntity = new UserEntity();
        userEntity.setId(uuid());
        userEntity.setUsername(username);
        userEntity.setNickname("zhangsan");
        userEntity.setEmail("zhangsan@qq.com");
        userEntity.setProvince("广东省");
        userEntity.setCity("深圳市");
        userEntity.setSex(SexEnum.MAN);
        userEntity.setWork("牛马程序猿");
        userEntity.setPhone("13245678911");
        userEntity.setStatus(AccountStatusEnum.ACTIVE);
        userEntity.setLastLoginTime(LocalDateTime.now());
        userEntity.setLocked(Boolean.FALSE);
        return userEntity;
    }

    private static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
