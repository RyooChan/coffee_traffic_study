package com.example.coffee.domain.coffee.facade;

import com.example.coffee.domain.coffee.dto.CoffeeBuyReq;
import com.example.coffee.domain.coffee.dto.CoffeePopularRes;
import com.example.coffee.domain.coffeeOrderCount.service.CoffeeOrderCountService;
import com.example.coffee.domain.exception.BusinessException;
import com.example.coffee.domain.exception.ExceptionCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CoffeeFacadeTest {
    @Autowired
    private CoffeeFacade coffeeFacade;
    @Autowired
    private CoffeeOrderCountService coffeeOrderCountService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @BeforeEach
    public void cntInit() {
    }

    @AfterEach
    public void cntDelete() {
        redisTemplate.keys("*").stream().forEach(k-> {
            redisTemplate.delete(k);
        });
    }

    @Test
    public void 커피_구매_테스트() throws InterruptedException {
        // given
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        CoffeeBuyReq coffeeBuyReq = new CoffeeBuyReq(1L, 1L);
        String[] ansName = {"americano"};
        Integer[] ansHit = {100};

        // when
        for(int i=0; i<threadCount; i++){
            executorService.submit(() ->{
                try{
                    coffeeFacade.coffeeBuy(coffeeBuyReq);
                } catch (InterruptedException e){
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // then
        ArrayList<CoffeePopularRes> hits = coffeeOrderCountService.findHits();
        for(int i=0; i<hits.size(); i++){
            assertThat(hits.get(i).getCoffeeName()).isEqualTo(ansName[i]);
            assertThat(hits.get(i).getHit()).isEqualTo(ansHit[i]);
        }
    }

    @Test
    public void 커피_구매_실패_테스트() throws InterruptedException {
        // given
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        CoffeeBuyReq coffeeBuyReq = new CoffeeBuyReq(5L, 1L);
        AtomicInteger errors = new AtomicInteger();
        Integer output = 29;

        // when

        for(int i=0; i<threadCount; i++) {
            executorService.submit(() -> {
                try {
                    coffeeFacade.coffeeBuy(coffeeBuyReq);
                } catch (BusinessException | InterruptedException e) {
                    errors.getAndIncrement();
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // then
        assertThat(errors.get()).isEqualTo(output);
    }

}