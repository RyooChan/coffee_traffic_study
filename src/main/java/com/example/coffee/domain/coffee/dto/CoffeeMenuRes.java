package com.example.coffee.domain.coffee.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"coffeeId", "coffeeName", "coffeePrice"})
public class CoffeeMenuRes implements Serializable {
    private Long coffeeId;
    private String coffeeName;
    private Integer coffeePrice;
}
