package com.shortly.apiservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shortly.apiservice.dto.response.ApiKeyPlanCache;
import com.shortly.apiservice.enumaration.ExceptionType;
import com.shortly.apiservice.exception.ApplicationException;
import com.shortly.apiservice.repository.ApiKeyRepository;
import com.shortly.apiservice.repository.projection.ApiKeyPlanProjection;
import com.shortly.apiservice.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ApiKeyRepository apiKeyRepository;
    private final ObjectMapper objectMapper;

    @Override
    public ApiKeyPlanCache getPlan(String apiKey) {

        String cacheKey = "plan:" + apiKey;

        // ===================
        // 1. GET FROM REDIS
        // ===================
        Object cached = redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            return objectMapper.convertValue(cached, ApiKeyPlanCache.class);
        }

        // ===================
        // 2. FALLBACK TO DB
        // ===================
        ApiKeyPlanProjection data = apiKeyRepository.findLimitByApiKey(apiKey)
                .orElseThrow(() -> new ApplicationException(
                        ExceptionType.BAD_REQUEST, "Invalid API key"
                ));

        // ===================
        // 3. MAP TO CACHE OBJECT
        // ===================
        ApiKeyPlanCache cache = ApiKeyPlanCache.builder()
                .maxRequestsPerDay(data.getMaxRequestsPerDay())
                .maxUrlsPerKey(data.getMaxUrlsPerKey())
                .build();

        redisTemplate.opsForValue().set(cacheKey, cache, Duration.ofHours(1));
        return cache;
    }
}
