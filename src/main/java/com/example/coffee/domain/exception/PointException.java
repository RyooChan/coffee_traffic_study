package com.example.coffee.domain.exception;

public class PointException extends BusinessException{
    public PointException() {
        super(ExceptionCode.POINT_USE_MINUS);
    }
}
