package com.example.coffee.domain.user.facade;

import com.example.coffee.domain.exception.BusinessException;
import com.example.coffee.domain.exception.ExceptionCode;
import com.example.coffee.domain.exception.PointException;
import com.example.coffee.domain.user.entity.User;
import com.example.coffee.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserFacadeTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired UserFacade userFacade;

    @BeforeEach
    public void userInsert() {
        User user = new User(1L, 50000);
        userRepository.saveAndFlush(user);
    }

    @AfterEach
    public void userDelete() {
        userRepository.deleteAll();
    }

    @Test
    public void 동시_동일_유저_포인트_적립() throws InterruptedException {
        // given
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for(int i=0; i<threadCount; i++){
            executorService.submit(() ->{
                try{
                    userFacade.pointCharge(1L, 500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        // then
        latch.await();

        User user = userRepository.findById(1L).orElseThrow();

        assertEquals(100000, user.getPoint());
    }

    @Test
    public void 동시_동일_유저_포인트_사용() throws InterruptedException {
        // given
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for(int i=0; i<threadCount; i++){
            executorService.submit(() ->{
                try{
                    userFacade.pointUse(1L, 500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        // then
        latch.await();

        User user = userRepository.findById(1L).orElseThrow();

        assertEquals(0, user.getPoint());
    }

    @Test
    public void 포인트_사용_초과() throws InterruptedException {
        // given
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        // then
        for(int i=0; i<threadCount; i++){
            executorService.submit(() -> {
                try{
                    userFacade.pointUse(1L, 600);
                } catch (BusinessException | InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        User user = userRepository.findById(1L).orElseThrow();
        assertEquals(200, user.getPoint());
    }

}