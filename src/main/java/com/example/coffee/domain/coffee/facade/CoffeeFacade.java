package com.example.coffee.domain.coffee.facade;

import com.example.coffee.domain.coffee.dto.CoffeeBuyReq;
import com.example.coffee.domain.coffee.dto.CoffeeDataDto;
import com.example.coffee.domain.coffee.entity.Coffee;
import com.example.coffee.domain.coffee.service.CoffeeService;
import com.example.coffee.domain.coffeeOrderCount.service.CoffeeOrderCountService;
import com.example.coffee.domain.user.facade.UserFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CoffeeFacade {
    private final UserFacade userFacade;
    private final CoffeeService coffeeService;
    private final CoffeeOrderCountService coffeeOrderCountService;

    public String coffeeBuy(CoffeeBuyReq coffeeBuyReq) throws InterruptedException {
        Coffee coffee = coffeeService.coffeeMenu(coffeeBuyReq.getCoffeeId());
        LocalDate localDate = LocalDate.now();
        CoffeeDataDto coffeeDataDto = new CoffeeDataDto(coffeeBuyReq.getUserId(), coffeeBuyReq.getCoffeeId(), coffee.getPrice());

        userFacade.pointUse(coffeeBuyReq.getUserId(), coffee.getPrice());
        coffeeOrderCountService.increaseHits(coffee.getName(), localDate);
        return coffeeService.callMockApiServer(coffeeDataDto);
    }
}
