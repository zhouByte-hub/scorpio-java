package com.zhoubyte.scorpioflowable.response;

import org.springframework.http.HttpStatus;

/**
 * 统一响应结果类
 */
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

    public static Result<String> error(String data) {
        return new Result<>(HttpStatus.BAD_REQUEST.value(), data, "error");
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
