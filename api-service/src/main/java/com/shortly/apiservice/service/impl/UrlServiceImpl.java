package com.shortly.apiservice.service.impl;

import com.shortly.apiservice.client.KgsClient;
import com.shortly.apiservice.dto.request.UrlRequest;
import com.shortly.apiservice.dto.response.UrlCache;
import com.shortly.apiservice.dto.response.UrlResponse;
import com.shortly.apiservice.entity.ApiKey;
import com.shortly.apiservice.entity.Url;
import com.shortly.apiservice.enumaration.ActionType;
import com.shortly.apiservice.enumaration.ExceptionType;
import com.shortly.apiservice.enumaration.TargetType;
import com.shortly.apiservice.exception.ApplicationException;
import com.shortly.apiservice.repository.ApiKeyRepository;
import com.shortly.apiservice.repository.UrlRepository;
import com.shortly.apiservice.service.AuditLogService;
import com.shortly.apiservice.service.QuotaService;
import com.shortly.apiservice.service.UrlService;
import com.shortly.apiservice.utils.ApiKeyHashUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlServiceImpl implements UrlService {

    private final UrlRepository urlRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final KgsClient kgsClient;
    private final AuditLogService auditLogService;
    private final QuotaService quotaService;

    @Value("${endpoint.app}")
    private String endpointApp;

    @Transactional
    @Override
    public UrlResponse createUrl(UrlRequest urlRequest, String apiKey, HttpServletRequest request) {

        // 1. VALIDATE API KEY EXPIRE
        String hashedKey = ApiKeyHashUtil.hash(apiKey);

        ApiKey key = apiKeyRepository.findByKeyHash(hashedKey);
        if (key == null || key.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApplicationException(ExceptionType.BAD_REQUEST, "Invalid or expired API key");
        }

        // 2. CHECK QUOTA
        quotaService.checkQuota(hashedKey);

        // 3. VALIDATE EXISTING URL
        String shortKey = urlRequest.getShortKey();
        if(shortKey != null && !shortKey.isBlank()) {
            if (urlRepository.existsByShortKey(shortKey)) {
                throw new ApplicationException(ExceptionType.BAD_REQUEST, "Short key already exists");
            }
        }

        // 4. CALLING KGS CLIENT
        if (shortKey == null || shortKey.isBlank()) {
            try {
                shortKey = kgsClient.getKey();
            } catch (Exception e) {
                log.error("Failed to get key from KGS", e);
                throw new RuntimeException("Failed to generate short key");
            }
        }

        // 5. HANDLE DUPLICATE (IMPORTANT)
        int retry = 0;
        while (urlRepository.existsByShortKey(shortKey)) {

            if (retry >= 3) {
                throw new RuntimeException("Failed to generate unique short key");
            }

            shortKey = kgsClient.getKey();
            retry++;
        }

        String shortedUrl = endpointApp + shortKey;

        // 6. SAVE TO DB (POSTGRES)
        Url url = Url.builder()
                .originalUrl(urlRequest.getOriginalUrl())
                .apiKey(key)
                .shortKey(shortKey)
                .user(auditLogService.getCurrentUser())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();

        Url save = urlRepository.save(url);

        // 7. CACHE
        String finalShortKey = shortKey;

        UrlCache cacheData = UrlCache.builder()
                .id(save.getId())
                .shortKey(save.getShortKey())
                .originalUrl(save.getOriginalUrl())
                .build();

        cacheUrl(finalShortKey, cacheData);

        // 8. SAVE TO AUDIT LOG
        auditLogService.saveAuditLog(
                request,
                ActionType.CREATE_SHORT_URL,
                TargetType.SHORT_URL,
                save.getId()

        );

        // 9. INCREMENT QUOTA IN REDIS
        quotaService.incrementQuota(hashedKey);

        // 11. RESPONSE
        return UrlResponse.builder()
                .id(save.getId())
                .originalUrl(save.getOriginalUrl())
                .shortedUrl(shortedUrl)
                .build();
    }

    @Override
    public void redirect(String shortKey, HttpServletResponse response) throws IOException {

        Url url = urlRepository.findByShortKey(shortKey)
                .orElseThrow(() -> new RuntimeException("URL not found"));
        if (url.getExpiresAt() != null &&
                url.getExpiresAt().isBefore(LocalDateTime.now())) {

            throw new RuntimeException("URL expired");
        }
        response.sendRedirect(url.getOriginalUrl());
    }

    @Async
    public void cacheUrl(String key, UrlCache cache) {
        redisTemplate.opsForValue().set(
                "url:" + key,
                cache,
                Duration.ofDays(7)
        );
    }
}
