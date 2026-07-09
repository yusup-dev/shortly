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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuotaServiceImpl implements QuotaService {

    private static final Duration QUOTA_CACHE_TTL = Duration.ofHours(1);

    private final StringRedisTemplate stringRedisTemplate;
    private final UrlRepository urlRepository;
    private final PlanService planService;

    @Override
    public void checkQuota(String apiKey) {
        long maxUrls = resolveMaxUrls(planService.getPlan(apiKey));
        long count = getOrSeedCounter(apiKey);

        if (count >= maxUrls) {
            throw new ApplicationException(ExceptionType.QUOTA_EXCEEDED);
        }

        log.debug("Quota apiKey={} count={}/{}", apiKey, count, maxUrls);
    }

    @Override
    public void incrementQuota(String apiKey) {
        String redisKey = buildKey(apiKey);
        Long count = stringRedisTemplate.opsForValue().increment(redisKey);

        if (count != null && count == 1) {
            stringRedisTemplate.expire(redisKey, QUOTA_CACHE_TTL);
        }

        if (count != null && count < 0) {
            stringRedisTemplate.opsForValue().set(redisKey, "0", QUOTA_CACHE_TTL);
        }
    }

    @Override
    public void decrementQuota(String apiKey) {
        String redisKey = buildKey(apiKey);
        Long count = stringRedisTemplate.opsForValue().decrement(redisKey);

        if (count != null && count < 0) {
            stringRedisTemplate.opsForValue().set(redisKey, "0", QUOTA_CACHE_TTL);
        }
    }

    private long getOrSeedCounter(String apiKey) {
        String redisKey = buildKey(apiKey);
        String cached = stringRedisTemplate.opsForValue().get(redisKey);

        if (cached != null) {
            try {
                return Long.parseLong(cached);
            } catch (NumberFormatException ex) {
                log.warn("Invalid quota counter for key={} value={}", redisKey, cached);
                stringRedisTemplate.delete(redisKey);
            }
        }

        long count = urlRepository.countByApiKeyHash(apiKey);
        stringRedisTemplate.opsForValue().set(redisKey, String.valueOf(count), QUOTA_CACHE_TTL);
        return count;
    }

    private long resolveMaxUrls(ApiKeyPlanCache plan) {
        Integer maxUrls = plan.getMaxUrlsPerKey();
        return maxUrls == null || maxUrls < 0 ? 0 : maxUrls;
    }

    private String buildKey(String apiKey) {
        return CacheConstants.CACHE_QUOTA + apiKey;
    }
}
