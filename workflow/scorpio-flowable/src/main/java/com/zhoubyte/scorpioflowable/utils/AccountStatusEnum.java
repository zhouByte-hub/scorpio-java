package com.zhoubyte.scorpioflowable.utils;

import lombok.Getter;

@Getter
public enum AccountStatusEnum {

    ACTIVE,             // 正常
    DISABLED,           // 停用
    LOCKED,             // 锁定
    UNACTIVATED,        // 未激活
    EXPIRED,            // 过期
    DELETED;            // 已删除
}
