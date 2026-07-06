package com.shortly.apiservice.service.impl;

import com.shortly.apiservice.constant.CacheConstants;
import com.shortly.apiservice.dto.response.ApiKeyPlanCache;
import com.shortly.apiservice.enumaration.ExceptionType;
import com.shortly.apiservice.exception.ApplicationException;
import com.shortly.apiservice.service.PlanService;
import com.shortly.apiservice.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitServiceImpl implements RateLimitService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final PlanService planService;

    @Override
    public void checkRateLimit(String apiKey) {

        // ===================
        // 1. GET PLAN (CACHE FIRST)
        // ===================
        ApiKeyPlanCache plan = planService.getPlan(apiKey);

        int maxRequests = plan.getMaxRequestsPerDay();

        // ===================
        // 2. BUILD REDIS KEY
        // ===================
        String redisKey = buildKey(apiKey);

        // ===================
        // 3. INCREMENT COUNTER
        // ===================
        Long count = redisTemplate.opsForValue().increment(redisKey);

        // ===================
        // 4. SET TTL (DAILY RESET)
        // ===================
        if (count != null && count == 1) {

            long secondsUntilMidnight = LocalDateTime.now()
                            .until(LocalDate.now().plusDays(1).atStartOfDay(), ChronoUnit.SECONDS);

            redisTemplate.expire(redisKey, Duration.ofSeconds(secondsUntilMidnight));
        }

        // ===================
        // 5. VALIDATE LIMIT
        // ===================
        if(count != null && count > maxRequests) {
            throw new ApplicationException(
                    ExceptionType.RATE_LIMIT_EXCEEDED
            );
        }
    }

    @Override
    public RateLimitStatus getStatus(String apiKey) {
        ApiKeyPlanCache plan = planService.getPlan(apiKey);
        int maxRequests = plan.getMaxRequestsPerDay();

        String redisKey = buildKey(apiKey);
        Object cached = redisTemplate.opsForValue().get(redisKey);
        long used = (cached instanceof Number number) ? number.longValue() : 0L;

        long remaining = Math.max(0, maxRequests - used);
        long resetEpochSeconds = LocalDate.now().plusDays(1)
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toEpochSecond();

        return new RateLimitService.RateLimitStatus(maxRequests, remaining, resetEpochSeconds);
    }

    private String buildKey(String apiKey) {
        return CacheConstants.CACHE_RATE_LIMIT + apiKey + ":" + LocalDate.now();
    }
}
