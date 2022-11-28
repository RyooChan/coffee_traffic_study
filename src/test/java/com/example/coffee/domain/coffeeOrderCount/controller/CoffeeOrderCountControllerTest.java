package com.example.coffee.domain.coffeeOrderCount.controller;

import com.example.coffee.domain.coffee.dto.CoffeeBuyReq;
import com.example.coffee.domain.coffee.dto.CoffeePopularRes;
import com.example.coffee.domain.coffeeOrderCount.service.CoffeeOrderCountService;
import com.example.coffee.domain.exception.BusinessException;
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

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class CoffeeOrderCountControllerTest {

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
    void 인기_확인() throws Exception {
        // given
        CoffeeBuyReq ameBuyReq = new CoffeeBuyReq(1L, 1L);
        CoffeeBuyReq latteBuyReq = new CoffeeBuyReq(2L, 1L);
        CoffeeBuyReq teaBuyReq = new CoffeeBuyReq(3L, 1L);
        String output = "{\"status\":200,\"message\":\"popularCoffees\",\"data\":[{\"coffeeName\":\"tea\",\"hit\":90},{\"coffeeName\":\"latte\",\"hit\":6},{\"coffeeName\":\"americano\",\"hit\":4}]}";

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for(int i=0; i<threadCount; i++){
            int finalI = i;
            executorService.submit(() ->{
                try{
                    if(finalI < 4){
                        mvc.perform(post("/api/v1/coffee/buy")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(ameBuyReq))
                                .accept(MediaType.APPLICATION_JSON)
                        );
                    } else if(finalI < 10){
                        mvc.perform(post("/api/v1/coffee/buy")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(latteBuyReq))
                                .accept(MediaType.APPLICATION_JSON)
                        );
                    } else{
                        mvc.perform(post("/api/v1/coffee/buy")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(teaBuyReq))
                                .accept(MediaType.APPLICATION_JSON)
                        );
                    }
                } catch (InterruptedException e){
                    throw new RuntimeException(e);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                } catch (Exception e) {
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
                .andExpect(content().string(output))
                .andDo(print());

    }

}