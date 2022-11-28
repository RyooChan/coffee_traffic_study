package com.example.coffee.domain.coffeeOrderCount.service;

import com.example.coffee.config.RedisKey;
import com.example.coffee.domain.coffee.dto.CoffeePopularRes;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class CoffeeOrderCountService {

    private final RedisTemplate<String, String> redisTemplate;

    public void increaseHits(String coffeeName, LocalDate localDate) throws InterruptedException {
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();

        zSetOperations.incrementScore(RedisKey.HIT.getName() + localDate.toString(), coffeeName, 1);
        zSetOperations.incrementScore(RedisKey.HIT.getName(), coffeeName, 1);
    }

    @Transactional(readOnly = true)
    public ArrayList<CoffeePopularRes> findHits() {
        ArrayList<CoffeePopularRes> coffees = new ArrayList<>();
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        Set<ZSetOperations.TypedTuple<String>> typedTuples = zSetOperations.reverseRangeWithScores(RedisKey.HIT.getName(), 0, 2);

        for (ZSetOperations.TypedTuple v : typedTuples) {
            coffees.add(new CoffeePopularRes(v.getValue().toString(), v.getScore().intValue()));
        }
        return coffees;
    }

}
