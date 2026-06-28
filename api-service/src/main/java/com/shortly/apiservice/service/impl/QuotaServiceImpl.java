package com.shortly.apiservice.service.impl;

import com.shortly.apiservice.constant.CacheConstants;
import com.shortly.apiservice.dto.response.ApiKeyPlanCache;
import com.shortly.apiservice.enumaration.ExceptionType;
import com.shortly.apiservice.exception.ApplicationException;
import com.shortly.apiservice.repository.UrlRepository;
import com.shortly.apiservice.service.PlanService;
import com.shortly.apiservice.service.QuotaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuotaServiceImpl implements QuotaService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final UrlRepository urlRepository;
    private final PlanService planService;

    @Override
    public void checkQuota(String apiKey) {
        // ===============
        // 1. GET PLAN (REDIS)
        // ===============
        ApiKeyPlanCache plan = planService.getPlan(apiKey);
        int maxUrls = plan.getMaxUrlsPerKey();

        String redisKey = buildKey(apiKey);

        // 2. GET COUNTER
        Long count = null;

        Object cachedCount = redisTemplate.opsForValue().get(redisKey);

        if (cachedCount instanceof Number number) {
            count = number.longValue();
        }
        // ===================
        // 3. FALLBACK KE DB (ONLY ONCE)
        // ===================
        if (count == null) {
            count = urlRepository.countByApiKeyHash(apiKey);

            redisTemplate.opsForValue().set(
                    redisKey,
                    count,
                    Duration.ofHours(1)
            );
        }

        // ===================
        // 4. VALIDATE
        // ===================
        if (count >= maxUrls) {
            throw new ApplicationException(
                    ExceptionType.BAD_REQUEST,
                    "Quota exceeded"
            );
        }

        log.debug("Quota apiKey={} count={}/{}", apiKey, count, maxUrls);
    }

    @Override
    public void incrementQuota(String apiKey) {
        String redisKey = buildKey(apiKey);
        Long count = redisTemplate.opsForValue().increment(redisKey);

        if (count != null && count < 0) {
            redisTemplate.opsForValue().set(redisKey, 0);
        }
    }

    @Override
    public void decrementQuota(String apiKey) {
        String redisKey = buildKey(apiKey);
        Long count = redisTemplate.opsForValue().decrement(redisKey);

        if(count != null && count < 0) {
            redisTemplate.opsForValue().set(redisKey, 0);
        }
    }

    private String buildKey(String apiKey) {
        return CacheConstants.CACHE_QUOTA + apiKey;
    }
}
