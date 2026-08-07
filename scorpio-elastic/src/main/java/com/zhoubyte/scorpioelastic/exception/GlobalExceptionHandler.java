package com.zhoubyte.scorpioelastic.exception;

import com.zhoubyte.scorpioelastic.common.Result;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ElasticBusinessException.class)
    public Result<Void> handleElasticBusinessException(ElasticBusinessException exception) {
        return Result.fail(exception.getMessage());
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            ConstraintViolationException.class
    })
    public Result<Void> handleValidationException(Exception exception) {
        return Result.fail(HttpStatus.BAD_REQUEST.value(), exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception exception) {
        return Result.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "系统异常，请稍后再试");
    }
}
