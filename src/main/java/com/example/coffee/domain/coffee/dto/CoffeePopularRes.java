package com.example.coffee.domain.coffee.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CoffeePopularRes {
    private String coffeeName;
    private Integer hit;
}
