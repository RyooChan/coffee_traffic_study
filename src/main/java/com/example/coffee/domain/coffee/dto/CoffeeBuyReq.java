package com.example.coffee.domain.coffee.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CoffeeBuyReq {
    private final Long coffeeId;
    private final Long userId;
}
