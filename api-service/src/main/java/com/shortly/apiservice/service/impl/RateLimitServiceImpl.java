package com.shortly.apiservice.service.impl;

import com.shortly.apiservice.constant.CacheConstants;
import com.shortly.apiservice.dto.response.ApiKeyPlanCache;
import com.shortly.apiservice.enumaration.ExceptionType;
import com.shortly.apiservice.exception.ApplicationException;
import com.shortly.apiservice.service.PlanService;
import com.shortly.apiservice.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitServiceImpl implements RateLimitService {

    private final StringRedisTemplate stringRedisTemplate;
    private final PlanService planService;

    @Override
    public void checkRateLimit(String apiKey) {
        long maxRequests = resolveMaxRequests(planService.getPlan(apiKey));
        String redisKey = buildKey(apiKey);

        Long count = stringRedisTemplate.opsForValue().increment(redisKey);

        if (count != null && count == 1) {
            long secondsUntilMidnight = LocalDateTime.now()
                    .until(LocalDate.now().plusDays(1).atStartOfDay(), ChronoUnit.SECONDS);
            stringRedisTemplate.expire(redisKey, Duration.ofSeconds(secondsUntilMidnight));
        }

        if (count != null && count > maxRequests) {
            throw new ApplicationException(ExceptionType.RATE_LIMIT_EXCEEDED);
        }

        log.debug("Rate limit apiKey={} count={}/{}", apiKey, count, maxRequests);
    }

    @Override
    public RateLimitStatus getStatus(String apiKey) {
        long maxRequests = resolveMaxRequests(planService.getPlan(apiKey));
        long used = readCounter(buildKey(apiKey));
        long remaining = Math.max(0, maxRequests - used);
        long resetEpochSeconds = LocalDate.now().plusDays(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toEpochSecond();

        return new RateLimitStatus(maxRequests, remaining, resetEpochSeconds);
    }

    private long resolveMaxRequests(ApiKeyPlanCache plan) {
        Integer maxRequests = plan.getMaxRequestsPerDay();
        return maxRequests == null || maxRequests < 0 ? 0 : maxRequests;
    }

    private long readCounter(String redisKey) {
        String cached = stringRedisTemplate.opsForValue().get(redisKey);
        if (cached == null) {
            return 0L;
        }

        try {
            return Long.parseLong(cached);
        } catch (NumberFormatException ex) {
            log.warn("Invalid rate-limit counter for key={} value={}", redisKey, cached);
            stringRedisTemplate.delete(redisKey);
            return 0L;
        }
    }

    private String buildKey(String apiKey) {
        return CacheConstants.CACHE_RATE_LIMIT + apiKey + ":" + LocalDate.now();
    }
}
