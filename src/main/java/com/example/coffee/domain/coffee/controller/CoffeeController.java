package com.example.coffee.domain.coffee.controller;

import com.example.coffee.domain.BaseResponse;
import com.example.coffee.domain.coffee.dto.CoffeeBuyReq;
import com.example.coffee.domain.coffee.facade.CoffeeFacade;
import com.example.coffee.domain.coffee.service.CoffeeService;
import com.example.coffee.domain.exception.BusinessException;
import com.example.coffee.domain.exception.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/coffee")
public class CoffeeController {
    private final CoffeeService coffeeService;
    private final CoffeeFacade coffeeFacade;

    @GetMapping
    public ResponseEntity<BaseResponse> coffeeMenuList() {
        BaseResponse baseResponse = BaseResponse.of(HttpStatus.OK, "menu");
        baseResponse.setData(coffeeService.coffeeMenuList());
        return ResponseEntity.ok(baseResponse);
    }

    @PostMapping("/buy")
    public ResponseEntity<BaseResponse> coffeeBuy(@RequestBody CoffeeBuyReq coffeeBuyReq) {
        BaseResponse baseResponse = BaseResponse.of(HttpStatus.OK, "coffeeBuy");
        try{
            baseResponse.setData(coffeeFacade.coffeeBuy(coffeeBuyReq));
        } catch (InterruptedException e) {
            throw new BusinessException(ExceptionCode.POINT_USE_MINUS);
        }
        return ResponseEntity.ok(baseResponse);
    }
}
