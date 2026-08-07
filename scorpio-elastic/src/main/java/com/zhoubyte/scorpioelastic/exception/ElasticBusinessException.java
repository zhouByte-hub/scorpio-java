package com.zhoubyte.scorpioelastic.exception;

public class ElasticBusinessException extends RuntimeException {

    public ElasticBusinessException(String message) {
        super(message);
    }

    public ElasticBusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
