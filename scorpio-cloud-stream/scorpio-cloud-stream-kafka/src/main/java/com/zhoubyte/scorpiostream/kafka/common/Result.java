package com.zhoubyte.scorpiostream.kafka.common;

import org.springframework.http.HttpStatus;

public record Result<T>(Integer code, T data, String message) {

    public static <T> Result<T> success(T data) {
        return new Result<>(HttpStatus.OK.value(), data, "success");
    }

    public static <T> Result<T> fail(String message) {
        return new Result<>(HttpStatus.BAD_REQUEST.value(), null, message);
    }
}
