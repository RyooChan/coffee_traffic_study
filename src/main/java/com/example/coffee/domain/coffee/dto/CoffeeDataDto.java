package com.example.coffee.domain.coffee.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CoffeeDataDto {
    private Long userId;
    private Long coffeeId;
    private Integer price;
}
