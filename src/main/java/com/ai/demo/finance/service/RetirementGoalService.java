package com.ai.demo.finance.service;

import com.ai.demo.finance.exception.InvalidOperationException;
import com.ai.demo.finance.exception.NotFoundResourceException;
import com.ai.demo.finance.model.cache.RetirementGoal;
import java.time.Duration;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RetirementGoalService {
    private final RedisTemplate<String, RetirementGoal> redisTemplate;
    private final Duration timeToLive;

    public RetirementGoalService(RedisTemplate<String, RetirementGoal> template,
            @Value("${redis.retirement-goal.ttl.mins:1440}") Long ttl) {
        this.redisTemplate = template;
        this.timeToLive = Duration.ofMinutes(ttl);
    }

    public void saveRetirementGoal(RetirementGoal retirementGoal) {
        if (retirementGoal == null) {
            throw new InvalidOperationException("retirementGoal is null");
        }
        redisTemplate.opsForValue().set(retirementGoal.getKey(), retirementGoal, timeToLive);
    }

    public RetirementGoal getRetirementGoal(Long userId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(String.valueOf(userId)))
                .orElseThrow(() -> new NotFoundResourceException("RetirementGoal not found for userId: " + userId));
    }
}
