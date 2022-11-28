package com.example.coffee.domain.coffee.service;

import com.example.coffee.domain.coffee.dto.CoffeeDataDto;
import com.example.coffee.domain.coffee.dto.CoffeeMenuRes;
import com.example.coffee.domain.coffee.entity.Coffee;
import com.example.coffee.domain.coffee.repository.CoffeeRepository;
import com.example.coffee.domain.user.entity.User;
import com.example.coffee.domain.user.facade.UserFacade;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CoffeeServiceTest {

    @Autowired
    private CoffeeRepository coffeeRepository;

    @Autowired
    UserFacade userFacade;

    @Autowired
    CoffeeService coffeeService;

    @BeforeEach
    @CachePut(value = "Coffee", key = "1L", cacheManager = "cacheManager")
    public void coffeeInsert() {
        Coffee ame = new Coffee(1L, "americano", 3000);
        coffeeRepository.saveAndFlush(ame);
    }

    @AfterEach
    @CacheEvict(value = "Coffee", allEntries = true)
    public void coffeeDelete() {
        coffeeRepository.deleteAll();
    }

    @Test
    public void 커피_메뉴확인_테스트() {
        // given
        int threadCount = 100;
        String output = "americano";
        List<List<CoffeeMenuRes>> tester = new ArrayList<>();

        // when
        for(int i=0; i<threadCount; i++){
            tester.add(coffeeService.coffeeMenuList());
        }

        // then
        for(int i=0; i<threadCount; i++){
            assertThat(tester.get(i).get(0).getCoffeeName()).isEqualTo(output);
        }
    }

    @Test
    public void 커피_단일메뉴_확인() {
        // given
        int threadCount = 100;
        String output = "americano";

        // when

        // then
        for(int i=0; i<threadCount; i++){
            assertThat(coffeeService.coffeeMenu(1L).getName()).isEqualTo(output);
        }
    }

    @Test
    public void mock_api_호출_테스트() {
        // given
        CoffeeDataDto coffeeDataDto = new CoffeeDataDto(1L, 1L, 20);
        String output = "result:OK";

        // when
        String returnData = coffeeService.callMockApiServer(coffeeDataDto);

        // then
        assertThat(returnData).isEqualTo(output);
    }
}