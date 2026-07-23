package com.zhoubyte.scorpioflowable.utils;

import lombok.Getter;

@Getter
public enum SexEnum {

    WOMAN(0, "女"),
    MAN(1, "男"),
    UNKNOWN(2, "未知");

    private final Integer code;
    private final String label;

    SexEnum(Integer code, String label) {
        this.code = code;
        this.label = label;
    }

}
