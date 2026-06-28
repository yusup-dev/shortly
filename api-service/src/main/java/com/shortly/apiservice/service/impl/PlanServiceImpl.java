package com.shortly.apiservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shortly.apiservice.constant.CacheConstants;
import com.shortly.apiservice.dto.response.ApiKeyPlanCache;
import com.shortly.apiservice.enumaration.ExceptionType;
import com.shortly.apiservice.exception.ApplicationException;
import com.shortly.apiservice.repository.ApiKeyRepository;
import com.shortly.apiservice.repository.projection.ApiKeyPlanProjection;
import com.shortly.apiservice.service.CacheService;
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
    private final CacheService cacheService;

    @Override
    public ApiKeyPlanCache getPlan(String apiKey) {

        String cacheKey = CacheConstants.CACHE_PLAN + apiKey;

        // ===================
        // 1. GET FROM REDIS
        // ===================
        return cacheService.get(cacheKey, ApiKeyPlanCache.class)
                .orElseGet(() -> {
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

                    // ===================
                    // 4. SAVE TO CACHE
                    // ===================
                    cacheService.put(
                            cacheKey,
                            cache,
                            Duration.ofHours(1)
                    );

                    return cache;
                });
    }
}
