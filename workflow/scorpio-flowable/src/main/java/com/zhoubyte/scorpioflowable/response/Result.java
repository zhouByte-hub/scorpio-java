package com.zhoubyte.scorpioflowable.response;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class Result<T> {

    private int code;
    private T data;
    private String message;

    private Result(int code, T data, String message){
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(HttpStatus.OK.value(), data, "success");
    }
}
