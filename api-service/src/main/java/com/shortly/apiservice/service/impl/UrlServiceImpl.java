package com.shortly.apiservice.service.impl;

import com.shortly.apiservice.client.KgsClient;
import com.shortly.apiservice.constant.CacheConstants;
import com.shortly.apiservice.dto.request.UpdateUrlRequest;
import com.shortly.apiservice.dto.request.UrlRequest;
import com.shortly.apiservice.dto.request.SearchUrlRequest;
import com.shortly.apiservice.dto.response.UrlCache;
import com.shortly.apiservice.dto.response.UrlResponse;
import com.shortly.apiservice.entity.ApiKey;
import com.shortly.apiservice.entity.Url;
import com.shortly.apiservice.entity.User;
import com.shortly.apiservice.enumaration.ActionType;
import com.shortly.apiservice.enumaration.ExceptionType;
import com.shortly.apiservice.enumaration.TargetType;
import com.shortly.apiservice.exception.ApplicationException;
import com.shortly.apiservice.repository.ApiKeyRepository;
import com.shortly.apiservice.repository.UrlRepository;
import com.shortly.apiservice.service.*;
import com.shortly.apiservice.utils.ApiKeyHashUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlServiceImpl implements UrlService {

    private final UrlRepository urlRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final KgsClient kgsClient;
    private final UserService userService;
    private final AuditLogService auditLogService;
    private final QuotaService quotaService;
    private final CacheService cacheService;

    @Value("${base.url}")
    private String baseUrl;

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
        if (shortKey != null && !shortKey.isBlank()) {
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

        String shortedUrl = baseUrl +  "/" + shortKey;

        // 6. SAVE TO DB (POSTGRES)
        Url url = Url.builder()
                .originalUrl(urlRequest.getOriginalUrl())
                .apiKey(key)
                .shortKey(shortKey)
                .user(userService.getCurrentUser())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();

        Url save = urlRepository.save(url);

        // 7. CACHE

        UrlCache cacheData = UrlCache.builder()
                .id(save.getId())
                .shortKey(save.getShortKey())
                .originalUrl(save.getOriginalUrl())
                .build();

        cacheService.put(
                CacheConstants.CACHE_URL + shortKey,
                cacheData,
                Duration.ofDays(7)
        );

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

        String cacheKey = CacheConstants.CACHE_URL + shortKey;

        // 1. TRY CACHE
        var cached = cacheService.get(cacheKey, UrlCache.class);
        if (cached.isPresent()) {
            log.debug("Cache hit for shortKey={}", shortKey);

            response.sendRedirect(cached.get().getOriginalUrl());
            return;
        }

        // 2. FALLBACK TO DB
        Url url = urlRepository.findByShortKey(shortKey)
                .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND, "URL not found"));
        if (url.getExpiresAt() != null &&
                url.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApplicationException(ExceptionType.BAD_REQUEST, "URL expired");
        }
        // 3. UPDATE CACHE
        UrlCache cache = UrlCache.builder()
                .id(url.getId())
                .shortKey(url.getShortKey())
                .originalUrl(url.getOriginalUrl())
                .build();
        cacheService.put(cacheKey, cache, Duration.ofDays(7));

        // 4. REDIRECT
        response.sendRedirect(url.getOriginalUrl());
    }

    @Override
    public Page<UrlResponse> findAll(SearchUrlRequest request) {
        User currentUser = userService.getCurrentUser();

        Pageable pageable = PageRequest.of(
                request.getPage() - 1,
                request.getLimit()
        );

        Page<Url> page;

        if(StringUtils.hasText(request.getSearch())) {
            page = urlRepository.findByUserAndOriginalUrlContainingIgnoreCase(
                    currentUser, request.getSearch(), pageable);
        } else {
            page = urlRepository.findByUser(
                    currentUser,
                    pageable
            );
        }

        return page.map(
                url -> UrlResponse.from(url, baseUrl)
        );
    }

    @Override
    public UrlResponse findOne(UUID id) {
        User currentUser = userService.getCurrentUser();

        Url url = urlRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() ->
                        new ApplicationException(
                                ExceptionType.RESOURCE_NOT_FOUND,
                                "URL not found"
                        ));

        return UrlResponse.from(url, baseUrl);
    }

    @Override
    public UrlResponse update(UUID id, UpdateUrlRequest updateUrlRequest, HttpServletRequest request) {
        User currentUser = userService.getCurrentUser();

        Url url = urlRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new ApplicationException(
                        ExceptionType.RESOURCE_NOT_FOUND, "URL not found"
                ));

        url.setOriginalUrl(updateUrlRequest.getOriginalUrl());

        Url updated = urlRepository.save(url);

        auditLogService.saveAuditLog(
                request,
                ActionType.UPDATE_SHORT_URL,
                TargetType.SHORT_URL,
                updated.getId()
        );
        // update cache
        cacheService.evict(
                CacheConstants.CACHE_URL + url.getShortKey()
        );

        return UrlResponse.from(
                updated, baseUrl
        );
    }

    @Override
    public void delete(UUID id, HttpServletRequest request) {
        User currentUser = userService.getCurrentUser();

        Url url = urlRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new ApplicationException(
                        ExceptionType.RESOURCE_NOT_FOUND, "URL not found"
                ));

        auditLogService.saveAuditLog(
                request,
                ActionType.DELETE_SHORT_URL,
                TargetType.SHORT_URL,
                url.getId()
        );

        // update cache
        cacheService.evict(
                CacheConstants.CACHE_URL + url.getShortKey()
        );

        urlRepository.delete(url);
    }

    // Admin

    @Override
    public Page<UrlResponse> findAllForAdmin(SearchUrlRequest request) {
        Pageable pageable = PageRequest.of(
                request.getPage() - 1,
                request.getLimit()
        );

        Page<Url> page;

        if(StringUtils.hasText(request.getSearch())) {
            page = urlRepository.findByOriginalUrlContainingIgnoreCase(
                    request.getSearch(), pageable);
        } else {
            page = urlRepository.findAll(
                    pageable
            );
        }

        return page.map(url -> UrlResponse.from(url, baseUrl));
    }

    @Override
    public UrlResponse findOneForAdmin(UUID id) {
        Url url = urlRepository.findById(id).orElseThrow(
                () -> new ApplicationException(
                        ExceptionType.RESOURCE_NOT_FOUND,
                        "URL not found"
                )
        );

        return UrlResponse.from(url, baseUrl);
    }

    @Override
    public UrlResponse updateForAdmin(UUID id, UpdateUrlRequest updateUrlRequest, HttpServletRequest request) {
        Url url = urlRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(
                        ExceptionType.RESOURCE_NOT_FOUND, "URL not found"
                        ));


        url.setOriginalUrl(updateUrlRequest.getOriginalUrl());

        Url updated = urlRepository.save(url);

        auditLogService.saveAuditLog(
                request,
                ActionType.UPDATE_SHORT_URL,
                TargetType.SHORT_URL,
                updated.getId()
        );

        // update cache
        cacheService.evict(
                CacheConstants.CACHE_URL + url.getShortKey()
        );

        return UrlResponse.from(
                updated, baseUrl
        );
    }

    @Override
    public void deleteForAdmin(UUID id, HttpServletRequest request) {
        Url url = urlRepository.findById(id).orElseThrow(
                () -> new ApplicationException(
                        ExceptionType.RESOURCE_NOT_FOUND,
                        "URL not found"
                )
        );

        auditLogService.saveAuditLog(
                request,
                ActionType.DELETE_SHORT_URL,
                TargetType.SHORT_URL,
                url.getId()
        );

        // update cache
        cacheService.evict(
                CacheConstants.CACHE_URL + url.getShortKey()
        );

        urlRepository.delete(url);
    }

}
