package com.example.coffee.domain;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@AllArgsConstructor
public class BaseResponse {
    private int status;
    private String message;
    private Object data;

    public BaseResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public static BaseResponse of(HttpStatus httpStatus, String message) {
        int status = Optional.ofNullable(httpStatus)
                .orElse(HttpStatus.OK)
                .value();
        return new BaseResponse(status, message);
    }

    public static BaseResponse of(HttpStatus httpStatus, String message, Object data) {
        int status = Optional.ofNullable(httpStatus)
                .orElse(HttpStatus.OK)
                .value();
        return new BaseResponse(status, message, data);
    }
}