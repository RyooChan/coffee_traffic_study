package com.example.coffee.domain.coffeeOrderCount.service;

import com.example.coffee.domain.coffee.dto.CoffeePopularRes;
import com.example.coffee.domain.coffeeOrderCount.scheduler.DecreaseScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CoffeeOrderCountServiceTest {

    @Autowired
    private CoffeeOrderCountService coffeeOrderCountService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private DecreaseScheduler decreaseScheduler;

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
    public void 인기_증가_테스트() throws InterruptedException {
        // given
        LocalDate day = LocalDate.now();
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        String[] ansName = {"티", "아메리카노", "라떼"};
        Integer[] ansHit = {32, 30, 20};

        // when
        for(int i=0; i<threadCount; i++){
            int finalI = i;
            executorService.submit(() ->{
                try{
                    if(finalI <30){
                        coffeeOrderCountService.increaseHits("아메리카노", day);
                    } else if(finalI<50){
                        coffeeOrderCountService.increaseHits("라떼", day);
                    } else if(finalI < 82){
                        coffeeOrderCountService.increaseHits("티", day);
                    } else{
                        coffeeOrderCountService.increaseHits("녹차", day);
                    }
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
    public void 인기_증가_테스트_동일값_key_내림차순() throws InterruptedException {
        // given
        LocalDate day = LocalDate.now();
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        String[] ansName = {"티", "아메리카노", "라떼"};
        Integer[] ansHit = {25, 25, 25};

        // when
        for(int i=0; i<threadCount; i++){
            int finalI = i;
            executorService.submit(() ->{
                try{
                    if(finalI < 25){
                        coffeeOrderCountService.increaseHits("아메리카노", day);
                    } else if(finalI<50){
                        coffeeOrderCountService.increaseHits("라떼", day);
                    } else if(finalI < 75){
                        coffeeOrderCountService.increaseHits("티", day);
                    } else{
                        coffeeOrderCountService.increaseHits("녹차", day);
                    }
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
    public void 인기검색_No_Data() {
        // given

        // when

        // then
        ArrayList<CoffeePopularRes> hits = coffeeOrderCountService.findHits();
        assertThat(hits.size()).isEqualTo(0);
    }

    @Test
    public void 인기_이전_제외_테스트() throws InterruptedException {
        // given
        LocalDate day = LocalDate.now();
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        String outputName = "아메리카노";
        Integer outputCnt = 0;

        // when
        for(int i=0; i<threadCount; i++){
            executorService.submit(() ->{
                try{
                    coffeeOrderCountService.increaseHits("아메리카노", day.minusDays(7));
                } catch (InterruptedException e){
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        try{
            decreaseScheduler.decreaseHits();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // then
        ArrayList<CoffeePopularRes> hits = coffeeOrderCountService.findHits();
        for(int i=0; i<hits.size(); i++){
            assertThat(hits.get(i).getCoffeeName()).isEqualTo(outputName);
            assertThat(hits.get(i).getHit()).isEqualTo(outputCnt);
        }
    }

}