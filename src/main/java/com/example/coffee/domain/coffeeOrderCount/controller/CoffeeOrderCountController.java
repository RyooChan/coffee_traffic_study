package com.example.coffee.domain.coffeeOrderCount.controller;

import com.example.coffee.domain.BaseResponse;
import com.example.coffee.domain.coffeeOrderCount.service.CoffeeOrderCountService;
import com.example.coffee.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/coffeeOrderCount")
public class CoffeeOrderCountController {
    private final CoffeeOrderCountService coffeeOrderCountService;

    @GetMapping
    public ResponseEntity<BaseResponse> findHits() {
        BaseResponse baseResponse = BaseResponse.of(HttpStatus.OK, "popularCoffees");
        try {
            baseResponse.setData(coffeeOrderCountService.findHits());
        } catch (BusinessException e){
            throw new BusinessException(e.getErrorCode());
        }
        return ResponseEntity.ok(baseResponse);
    }
}
