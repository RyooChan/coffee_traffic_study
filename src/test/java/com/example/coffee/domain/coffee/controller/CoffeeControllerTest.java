package com.example.coffee.domain.coffee.controller;

import com.example.coffee.domain.coffee.dto.CoffeeBuyReq;
import com.example.coffee.domain.exception.BusinessException;
import com.example.coffee.domain.exception.ExceptionCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.net.BindException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class CoffeeControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @AfterEach
    public void cacheDelete() {
        redisTemplate.keys("*").stream().forEach(k-> {
            redisTemplate.delete(k);
        });
    }

    @Test
    void 커피_리스트_조회() throws Exception {
        // given
        String output = "{\"status\":200,\"message\":\"menu\",\"data\":[{\"coffeeId\":1,\"coffeeName\":\"americano\",\"coffeePrice\":3000},{\"coffeeId\":2,\"coffeeName\":\"latte\",\"coffeePrice\":4000},{\"coffeeId\":3,\"coffeeName\":\"tea\",\"coffeePrice\":5000},{\"coffeeId\":4,\"coffeeName\":\"ade\",\"coffeePrice\":6000},{\"coffeeId\":5,\"coffeeName\":\"espresso\",\"coffeePrice\":7000}]}";

        // when
        ResultActions perform = mvc.perform(get("/api/v1/coffee"));

        // then
        perform.andExpect(status().isOk())
            .andExpect(content().string(output))
            .andDo(print());
    }

    @Test
    void 구매_성공() throws Exception {
        // given
        CoffeeBuyReq coffeeBuyReq = new CoffeeBuyReq(1L, 1L);
        String output = "{\"status\":200,\"message\":\"coffeeBuy\",\"data\":\"{\\n    \\\"result\\\":\\\"OK\\\"\\n}\"}";

        // when
        ResultActions perform = mvc.perform(post("/api/v1/coffee/buy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(coffeeBuyReq))
                .accept(MediaType.APPLICATION_JSON)
        );

        // then
        perform.andExpect(status().isOk())
                .andExpect(content().string(output))
                .andDo(print());
    }

    @Test()
    void 구매_확인_실패_인기_확인() throws Exception {
        // given
        CoffeeBuyReq ameBuyReq = new CoffeeBuyReq(5L, 1L);

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger errors = new AtomicInteger();
        Integer outputCnt = 29;
        String outputString = "{\"status\":200,\"message\":\"popularCoffees\",\"data\":[{\"coffeeName\":\"espresso\",\"hit\":71}]}";

        // when
        for(int i=0; i<threadCount; i++){
            executorService.submit(() ->{
                try{
                    mvc.perform(post("/api/v1/coffee/buy")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(ameBuyReq))
                            .accept(MediaType.APPLICATION_JSON)
                    );
                } catch (Exception e) {
                    errors.getAndIncrement();
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        ResultActions perform = mvc.perform(get("/api/v1/coffeeOrderCount"));

        // then

        perform.andExpect(status().isOk())
                .andExpect(content().string(outputString))
                .andDo(print());

        assertThat(errors.get()).isEqualTo(outputCnt);
    }
}