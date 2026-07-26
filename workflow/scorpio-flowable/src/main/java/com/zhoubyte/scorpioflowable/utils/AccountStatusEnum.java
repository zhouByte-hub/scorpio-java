package com.zhoubyte.scorpioflowable.utils;

/**
 * 账户状态枚举
 */
public enum AccountStatusEnum {

    ACTIVE,             // 正常
    DISABLED,           // 停用
    LOCKED,             // 锁定
    UNACTIVATED,        // 未激活
    EXPIRED,            // 过期
    DELETED;            // 已删除
}
