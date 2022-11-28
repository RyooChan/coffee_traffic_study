package com.example.coffee.domain.user.controller;

import com.example.coffee.domain.coffee.dto.CoffeeBuyReq;
import com.example.coffee.domain.user.dto.UserPointChargeReq;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class UserControllerTest {

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
    void 포인트_충전() throws Exception {
        // given
        UserPointChargeReq userPointChargeReq = new UserPointChargeReq(1L, 50000);
        String output = "{\"status\":200,\"message\":\"menu\",\"data\":{\"userId\":1,\"point\":550000}}";

        // when
        ResultActions perform = mvc.perform(post("/api/v1/user/point")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userPointChargeReq))
                .accept(MediaType.APPLICATION_JSON)
        );

        // then
        perform.andExpect(status().isOk())
                .andExpect(content().string(output))
                .andDo(print());
    }


}