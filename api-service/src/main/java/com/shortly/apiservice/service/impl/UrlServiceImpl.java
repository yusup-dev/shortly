package com.shortly.apiservice.service.impl;

import com.shortly.apiservice.client.KgsClient;
import com.shortly.apiservice.constant.CacheConstants;
import com.shortly.apiservice.dto.request.BulkUrlItemRequest;
import com.shortly.apiservice.dto.request.BulkUrlRequest;
import com.shortly.apiservice.dto.request.UpdateExpiryRequest;
import com.shortly.apiservice.dto.request.UpdateUrlRequest;
import com.shortly.apiservice.dto.request.UrlRequest;
import com.shortly.apiservice.dto.request.SearchUrlRequest;
import com.shortly.apiservice.dto.response.BulkUrlResponse;
import com.shortly.apiservice.dto.response.BulkUrlResultItem;
import com.shortly.apiservice.dto.response.UrlCache;
import com.shortly.apiservice.dto.response.UrlResponse;
import com.shortly.apiservice.entity.ApiKey;
import com.shortly.apiservice.entity.Url;
import com.shortly.apiservice.entity.User;
import com.shortly.apiservice.enumaration.ActionType;
import com.shortly.apiservice.enumaration.ExceptionType;
import com.shortly.apiservice.enumaration.KeyStatusType;
import com.shortly.apiservice.enumaration.PlanType;
import com.shortly.apiservice.enumaration.StatusType;
import com.shortly.apiservice.enumaration.TargetType;
import com.shortly.apiservice.exception.ApplicationException;
import com.shortly.apiservice.repository.ApiKeyRepository;
import com.shortly.apiservice.repository.UrlRepository;
import com.shortly.apiservice.service.*;
import com.shortly.apiservice.utils.ApiKeyHashUtil;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    private final PlanService planService;
    private final AnalyticsService analyticsService;

    private static final Duration DEFAULT_EXPIRY = Duration.ofDays(7);

    @Value("${base.url}")
    private String baseUrl;

    @Transactional
    @Override
    public UrlResponse createUrl(UrlRequest urlRequest, String apiKey, HttpServletRequest request) {

        String hashedKey = ApiKeyHashUtil.hash(apiKey);
        ApiKey key = validateApiKey(hashedKey);
        User currentUser = userService.getCurrentUser();

        quotaService.checkQuota(hashedKey);

        Url url = buildUrl(urlRequest, key, currentUser);
        Url saved = urlRepository.save(url);

        cacheUrl(saved);

        auditLogService.saveAuditLog(
                request,
                ActionType.CREATE_SHORT_URL,
                TargetType.SHORT_URL,
                saved.getId()
        );

        quotaService.incrementQuota(hashedKey);

        return UrlResponse.from(saved, baseUrl, 0L);
    }

    @Transactional
    @Override
    public BulkUrlResponse createBulk(BulkUrlRequest bulkUrlRequest, String apiKey, HttpServletRequest request) {

        String hashedKey = ApiKeyHashUtil.hash(apiKey);
        ApiKey key = validateApiKey(hashedKey);
        User currentUser = userService.getCurrentUser();

        if (currentUser.getPlan() == null || currentUser.getPlan().getName() != PlanType.PRO) {
            throw new ApplicationException(ExceptionType.NOT_PRO_PLAN, "Bulk shorten hanya untuk plan Pro");
        }

        List<BulkUrlItemRequest> items = bulkUrlRequest.getUrls() == null ? List.of() : bulkUrlRequest.getUrls();

        int maxBulk = planService.getPlan(hashedKey).getMaxBulk() == null
                ? 0
                : planService.getPlan(hashedKey).getMaxBulk();

        if (items.size() > maxBulk) {
            throw new ApplicationException(
                    ExceptionType.BULK_LIMIT_EXCEEDED,
                    "Maksimal " + maxBulk + " URL per request untuk plan kamu"
            );
        }

        List<BulkUrlResultItem> results = new ArrayList<>();
        int succeeded = 0;
        int failed = 0;

        for (int i = 0; i < items.size(); i++) {
            BulkUrlItemRequest item = items.get(i);
            try {
                UrlRequest single = new UrlRequest();
                single.setOriginalUrl(item.getOriginalUrl());
                single.setAlias(item.getAlias());
                single.setExpireAt(item.getExpireAt());

                quotaService.checkQuota(hashedKey);

                Url url = buildUrl(single, key, currentUser);
                Url saved = urlRepository.save(url);
                cacheUrl(saved);
                quotaService.incrementQuota(hashedKey);

                results.add(BulkUrlResultItem.builder()
                        .index(i)
                        .status("success")
                        .shortUrl(baseUrl + "/" + saved.getShortKey())
                        .build());
                succeeded++;

            } catch (ApplicationException ex) {
                results.add(BulkUrlResultItem.builder()
                        .index(i)
                        .status("failed")
                        .error(BulkUrlResultItem.BulkUrlError.builder()
                                .code(ex.getType().name())
                                .message(ex.getMessage())
                                .build())
                        .build());
                failed++;
            } catch (Exception ex) {
                log.error("Unexpected error while creating bulk url at index={}", i, ex);
                results.add(BulkUrlResultItem.builder()
                        .index(i)
                        .status("failed")
                        .error(BulkUrlResultItem.BulkUrlError.builder()
                                .code("INTERNAL_SERVER_ERROR")
                                .message("Unexpected error")
                                .build())
                        .build());
                failed++;
            }
        }

        auditLogService.saveAuditLog(
                request,
                ActionType.CREATE_SHORT_URL,
                TargetType.SHORT_URL,
                null
        );

        return BulkUrlResponse.builder()
                .total(items.size())
                .succeeded(succeeded)
                .failed(failed)
                .results(results)
                .build();
    }

    private ApiKey validateApiKey(String hashedKey) {
        ApiKey key = apiKeyRepository.findByKeyHash(hashedKey);
        if (key == null
                || key.getStatus() != KeyStatusType.ACTIVE
                || key.getExpiresAt() == null
                || key.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApplicationException(ExceptionType.INVALID_API_KEY);
        }
        return key;
    }

    private Url buildUrl(UrlRequest urlRequest, ApiKey key, User currentUser) {

        String originalUrl = urlRequest.getOriginalUrl();
        if (!isValidUrl(originalUrl)) {
            throw new ApplicationException(ExceptionType.INVALID_URL);
        }

        boolean isPro = currentUser.getPlan() != null && currentUser.getPlan().getName() == PlanType.PRO;

        String alias = urlRequest.getAlias();
        String shortKey;

        if (StringUtils.hasText(alias)) {
            if (!isPro) {
                throw new ApplicationException(ExceptionType.NOT_PRO_PLAN, "Custom alias hanya untuk plan Pro");
            }
            if (urlRepository.existsByShortKey(alias)) {
                throw new ApplicationException(
                        ExceptionType.ALIAS_ALREADY_TAKEN,
                        "Alias '" + alias + "' sudah digunakan"
                );
            }
            shortKey = alias;
        } else {
            shortKey = generateUniqueKey();
        }

        LocalDateTime expiresAt;
        if (urlRequest.getExpireAt() != null) {
            if (!isPro) {
                throw new ApplicationException(ExceptionType.NOT_PRO_PLAN, "Custom expiry hanya untuk plan Pro");
            }
            expiresAt = urlRequest.getExpireAt();
        } else {
            expiresAt = LocalDateTime.now().plus(DEFAULT_EXPIRY);
        }

        return Url.builder()
                .originalUrl(originalUrl)
                .apiKey(key)
                .shortKey(shortKey)
                .user(currentUser)
                .status(StatusType.ACTIVE)
                .expiresAt(expiresAt)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private String generateUniqueKey() {
        String shortKey;
        int retry = 0;
        do {
            if (retry >= 3) {
                throw new RuntimeException("Failed to generate unique short key");
            }
            try {
                shortKey = kgsClient.getKey();
            } catch (Exception e) {
                log.error("Failed to get key from KGS", e);
                throw new RuntimeException("Failed to generate short key");
            }
            retry++;
        } while (urlRepository.existsByShortKey(shortKey));

        return shortKey;
    }

    private boolean isValidUrl(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        try {
            URI uri = URI.create(value);
            return uri.isAbsolute() && (uri.getScheme().equals("http") || uri.getScheme().equals("https"));
        } catch (Exception e) {
            return false;
        }
    }

    private void cacheUrl(Url url) {
        UrlCache cacheData = UrlCache.builder()
                .id(url.getId())
                .shortKey(url.getShortKey())
                .originalUrl(url.getOriginalUrl())
                .status(url.getStatus() != null ? url.getStatus().name() : StatusType.ACTIVE.name())
                .expiresAt(url.getExpiresAt())
                .build();

        Duration ttl = DEFAULT_EXPIRY;
        if (url.getExpiresAt() != null) {
            Duration untilExpiry = Duration.between(LocalDateTime.now(), url.getExpiresAt());
            if (!untilExpiry.isNegative() && !untilExpiry.isZero()) {
                ttl = untilExpiry;
            }
        }

        cacheService.put(CacheConstants.CACHE_URL + url.getShortKey(), cacheData, ttl);
    }

    @Override
    public void redirect(String shortKey, HttpServletRequest servletRequest, HttpServletResponse response) throws IOException {

        String cacheKey = CacheConstants.CACHE_URL + shortKey;
        String ipAddress = getClientIp(servletRequest);
        String userAgent = servletRequest.getHeader("User-Agent");
        String referer = servletRequest.getHeader("Referer");

        UUID urlId;
        String originalUrl;

        var cached = cacheService.get(cacheKey, UrlCache.class);
        if (cached.isPresent()) {
            UrlCache cache = cached.get();
            assertRedirectable(cache.getStatus(), cache.getExpiresAt());

            urlId = cache.getId();
            originalUrl = cache.getOriginalUrl();
        } else {
            Url url = urlRepository.findByShortKey(shortKey)
                    .orElseThrow(() -> new ApplicationException(ExceptionType.SHORT_URL_NOT_FOUND));

            assertRedirectable(
                    url.getStatus() != null ? url.getStatus().name() : StatusType.ACTIVE.name(),
                    url.getExpiresAt()
            );

            cacheUrl(url);

            urlId = url.getId();
            originalUrl = url.getOriginalUrl();
        }

        analyticsService.recordClickAsync(urlId, ipAddress, userAgent, referer);

        response.setHeader("Cache-Control", "no-store");
        response.sendRedirect(originalUrl);
    }

    private void assertRedirectable(String status, LocalDateTime expiresAt) {
        if (StatusType.SUSPENDED.name().equals(status)) {
            throw new ApplicationException(ExceptionType.URL_SUSPENDED);
        }
        if (expiresAt != null && expiresAt.isBefore(LocalDateTime.now())) {
            throw new ApplicationException(ExceptionType.URL_EXPIRED);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Override
    public Page<UrlResponse> findAll(SearchUrlRequest request) {
        User currentUser = userService.getCurrentUser();

        Pageable pageable = PageRequest.of(
                request.getPage() - 1,
                request.getLimit(),
                buildSort(request.getSort())
        );

        Specification<Url> spec = buildSpecification(currentUser, request);

        Page<Url> page = urlRepository.findAll(spec, pageable);

        return page.map(url -> UrlResponse.from(url, baseUrl, analyticsService.getTotalClicks(url.getId())));
    }

    private Specification<Url> buildSpecification(User user, SearchUrlRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (user != null) {
                predicates.add(cb.equal(root.get("user"), user));
            }

            if (StringUtils.hasText(request.getSearch())) {
                predicates.add(cb.like(cb.lower(root.get("originalUrl")), "%" + request.getSearch().toLowerCase() + "%"));
            }

            if (StringUtils.hasText(request.getStatus())) {
                LocalDateTime now = LocalDateTime.now();
                switch (request.getStatus().toLowerCase()) {
                    case "suspended" -> predicates.add(cb.equal(root.get("status"), StatusType.SUSPENDED));
                    case "active" -> {
                        predicates.add(cb.equal(root.get("status"), StatusType.ACTIVE));
                        predicates.add(cb.or(
                                cb.isNull(root.get("expiresAt")),
                                cb.greaterThan(root.get("expiresAt"), now)
                        ));
                    }
                    case "expired" -> {
                        predicates.add(cb.equal(root.get("status"), StatusType.ACTIVE));
                        predicates.add(cb.isNotNull(root.get("expiresAt")));
                        predicates.add(cb.lessThanOrEqualTo(root.get("expiresAt"), now));
                    }
                    default -> {
                        // unknown filter value, ignore
                    }
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Sort buildSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        String[] parts = sort.split(":");
        String field = switch (parts[0]) {
            case "expires_at" -> "expiresAt";
            case "created_at" -> "createdAt";
            default -> "createdAt";
        };

        Sort.Direction direction = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1]))
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return Sort.by(direction, field);
    }

    @Override
    public UrlResponse findOne(UUID id) {
        User currentUser = userService.getCurrentUser();

        Url url = urlRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() ->
                        new ApplicationException(ExceptionType.SHORT_URL_NOT_FOUND));

        return UrlResponse.from(url, baseUrl, analyticsService.getTotalClicks(url.getId()));
    }

    @Override
    public UrlResponse updateExpiry(UUID id, UpdateExpiryRequest updateExpiryRequest, HttpServletRequest request) {
        User currentUser = userService.getCurrentUser();

        Url url = urlRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new ApplicationException(ExceptionType.SHORT_URL_NOT_FOUND));

        url.setExpiresAt(updateExpiryRequest.getExpireAt());

        Url updated = urlRepository.save(url);

        auditLogService.saveAuditLog(
                request,
                ActionType.UPDATE_SHORT_URL,
                TargetType.SHORT_URL,
                updated.getId()
        );

        cacheUrl(updated);

        return UrlResponse.from(updated, baseUrl, analyticsService.getTotalClicks(updated.getId()));
    }

    @Override
    public void delete(UUID id, HttpServletRequest request) {
        User currentUser = userService.getCurrentUser();

        Url url = urlRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new ApplicationException(ExceptionType.SHORT_URL_NOT_FOUND));

        auditLogService.saveAuditLog(
                request,
                ActionType.DELETE_SHORT_URL,
                TargetType.SHORT_URL,
                url.getId()
        );

        cacheService.evict(CacheConstants.CACHE_URL + url.getShortKey());

        urlRepository.delete(url);

        decrementQuotaForUrl(url);
    }

    private void decrementQuotaForUrl(Url url) {
        if (url.getApiKey() != null && url.getApiKey().getKeyHash() != null) {
            quotaService.decrementQuota(url.getApiKey().getKeyHash());
        }
    }

    // Admin

    @Override
    public Page<UrlResponse> findAllForAdmin(SearchUrlRequest request) {
        Pageable pageable = PageRequest.of(
                request.getPage() - 1,
                request.getLimit(),
                buildSort(request.getSort())
        );

        Specification<Url> spec = buildSpecification(null, request);

        Page<Url> page = urlRepository.findAll(spec, pageable);

        return page.map(url -> UrlResponse.from(url, baseUrl, analyticsService.getTotalClicks(url.getId())));
    }

    @Override
    public UrlResponse findOneForAdmin(UUID id) {
        Url url = urlRepository.findById(id).orElseThrow(
                () -> new ApplicationException(ExceptionType.SHORT_URL_NOT_FOUND)
        );

        return UrlResponse.from(url, baseUrl, analyticsService.getTotalClicks(url.getId()));
    }

    @Override
    public UrlResponse updateForAdmin(UUID id, UpdateUrlRequest updateUrlRequest, HttpServletRequest request) {
        Url url = urlRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ExceptionType.SHORT_URL_NOT_FOUND));

        url.setOriginalUrl(updateUrlRequest.getOriginalUrl());

        Url updated = urlRepository.save(url);

        auditLogService.saveAuditLog(
                request,
                ActionType.UPDATE_SHORT_URL,
                TargetType.SHORT_URL,
                updated.getId()
        );

        cacheUrl(updated);

        return UrlResponse.from(updated, baseUrl, analyticsService.getTotalClicks(updated.getId()));
    }

    @Override
    public void deleteForAdmin(UUID id, HttpServletRequest request) {
        Url url = urlRepository.findById(id).orElseThrow(
                () -> new ApplicationException(ExceptionType.SHORT_URL_NOT_FOUND)
        );

        auditLogService.saveAuditLog(
                request,
                ActionType.DELETE_SHORT_URL,
                TargetType.SHORT_URL,
                url.getId()
        );

        cacheService.evict(CacheConstants.CACHE_URL + url.getShortKey());

        urlRepository.delete(url);

        decrementQuotaForUrl(url);
    }

    @Override
    public UrlResponse updateStatusForAdmin(UUID id, String status, String reason, HttpServletRequest request) {
        Url url = urlRepository.findById(id).orElseThrow(
                () -> new ApplicationException(ExceptionType.SHORT_URL_NOT_FOUND)
        );

        StatusType newStatus;
        try {
            newStatus = StatusType.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApplicationException(ExceptionType.VALIDATION_ERROR, "Status tidak valid");
        }

        url.setStatus(newStatus);
        url.setSuspendedReason(newStatus == StatusType.SUSPENDED ? reason : null);

        Url updated = urlRepository.save(url);

        auditLogService.saveAuditLog(
                request,
                newStatus == StatusType.SUSPENDED ? ActionType.SUSPEND_URL : ActionType.ACTIVATE_URL,
                TargetType.SHORT_URL,
                updated.getId()
        );

        cacheUrl(updated);

        return UrlResponse.from(updated, baseUrl, analyticsService.getTotalClicks(updated.getId()));
    }

}
