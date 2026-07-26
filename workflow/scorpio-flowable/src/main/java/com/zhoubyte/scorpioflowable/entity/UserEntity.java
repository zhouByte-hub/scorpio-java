package com.zhoubyte.scorpioflowable.entity;

import com.zhoubyte.scorpioflowable.utils.AccountStatusEnum;
import com.zhoubyte.scorpioflowable.utils.SexEnum;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

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

    public UserEntity() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public SexEnum getSex() {
        return sex;
    }

    public void setSex(SexEnum sex) {
        this.sex = sex;
    }

    public String getWork() {
        return work;
    }

    public void setWork(String work) {
        this.work = work;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public AccountStatusEnum getStatus() {
        return status;
    }

    public void setStatus(AccountStatusEnum status) {
        this.status = status;
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

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