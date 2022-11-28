package com.example.coffee.domain.coffeeOrderCount.scheduler;

import com.example.coffee.config.RedisKey;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DecreaseScheduler {

    private final RedisTemplate<String, String> redisTemplate;

    @Scheduled(cron = "0 0 0 * * *")
    public void decreaseHits() throws InterruptedException {
        LocalDate localDate = LocalDate.now();
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        Set<ZSetOperations.TypedTuple<String>> rankReverseSet = zSetOperations.reverseRangeWithScores(RedisKey.HIT.getName() + localDate.minusDays(7), 0, -1);

        for (ZSetOperations.TypedTuple v : rankReverseSet) {
            zSetOperations.incrementScore(RedisKey.HIT.getName(), v.getValue().toString(), -v.getScore());
        }
    }

}
