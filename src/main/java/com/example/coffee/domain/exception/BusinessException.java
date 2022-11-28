package com.example.coffee.domain.exception;

public class BusinessException extends RuntimeException {

    private final ExceptionCode exceptionCode;

    public BusinessException(String message, ExceptionCode exceptionCode) {
        super(message);
        this.exceptionCode = exceptionCode;
    }

    public BusinessException(ExceptionCode exceptionCode) {
        super(exceptionCode.getMessage());
        this.exceptionCode = exceptionCode;
    }

    public ExceptionCode getErrorCode() {
        return exceptionCode;
    }

}