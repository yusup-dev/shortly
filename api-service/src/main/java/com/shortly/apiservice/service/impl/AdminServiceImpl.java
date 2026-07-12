package com.shortly.apiservice.service.impl;

import com.shortly.apiservice.constant.CacheConstants;
import com.shortly.apiservice.dto.request.SearchUserRequest;
import com.shortly.apiservice.dto.request.UpdateQuotaRequest;
import com.shortly.apiservice.dto.response.AdminMetricsResponse;
import com.shortly.apiservice.dto.response.AdminUserResponse;
import com.shortly.apiservice.entity.ApiKey;
import com.shortly.apiservice.entity.Quota;
import com.shortly.apiservice.entity.User;
import com.shortly.apiservice.enumaration.ActionType;
import com.shortly.apiservice.enumaration.ExceptionType;
import com.shortly.apiservice.enumaration.KeyStatusType;
import com.shortly.apiservice.enumaration.StatusType;
import com.shortly.apiservice.enumaration.TargetType;
import com.shortly.apiservice.exception.ApplicationException;
import com.shortly.apiservice.repository.ApiKeyRepository;
import com.shortly.apiservice.repository.QuotaRepository;
import com.shortly.apiservice.repository.UrlClickRepository;
import com.shortly.apiservice.repository.UrlRepository;
import com.shortly.apiservice.repository.UserRepository;
import com.shortly.apiservice.service.AdminService;
import com.shortly.apiservice.service.AuditLogService;
import com.shortly.apiservice.service.CacheService;
import com.shortly.apiservice.service.RefreshTokenService;
import com.shortly.apiservice.service.UserService;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final UrlRepository urlRepository;
    private final UrlClickRepository urlClickRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final QuotaRepository quotaRepository;
    private final AuditLogService auditLogService;
    private final CacheService cacheService;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    private static final Set<StatusType> ALLOWED_USER_STATUS_CHANGES = Set.of(
            StatusType.ACTIVE, StatusType.SUSPENDED
    );

    @Override
    public AdminMetricsResponse getMetrics() {
        LocalDateTime now = LocalDateTime.now();

        long totalUsers = userRepository.countByDeletedAtIsNull();
        long activeUsers = userRepository.countByStatusAndDeletedAtIsNull(StatusType.ACTIVE);
        long suspendedUsers = userRepository.countByStatusAndDeletedAtIsNull(StatusType.SUSPENDED);
        long inactiveUsers = userRepository.countByStatusAndDeletedAtIsNull(StatusType.INACTIVE);

        long totalUrls = urlRepository.countAll();
        long activeUrls = urlRepository.countActive(now);
        long expiredUrls = urlRepository.countExpired(now);
        long suspendedUrls = urlRepository.countSuspended();

        long totalClicks = urlClickRepository.countAllTime();
        long todayClicks = urlClickRepository.countToday();

        long totalApiKeys = apiKeyRepository.countAll();
        long activeApiKeys = apiKeyRepository.countActive();
        long inactiveApiKeys = totalApiKeys - activeApiKeys;

        return AdminMetricsResponse.builder()
                .users(AdminMetricsResponse.UserMetrics.builder()
                        .total(totalUsers)
                        .active(activeUsers)
                        .suspended(suspendedUsers)
                        .inactive(inactiveUsers)
                        .build())
                .urls(AdminMetricsResponse.UrlMetrics.builder()
                        .total(totalUrls)
                        .active(activeUrls)
                        .expired(expiredUrls)
                        .suspended(suspendedUrls)
                        .build())
                .clicks(AdminMetricsResponse.ClickMetrics.builder()
                        .total(totalClicks)
                        .today(todayClicks)
                        .build())
                .apiKeys(AdminMetricsResponse.ApiKeyMetrics.builder()
                        .total(totalApiKeys)
                        .active(activeApiKeys)
                        .inactive(inactiveApiKeys)
                        .build())
                .build();
    }

    @Override
    @Transactional
    public Page<AdminUserResponse> findAllUsers(SearchUserRequest request) {
        Pageable pageable = buildPageable(request);
        Specification<User> spec = buildUserSpecification(request);
        Page<User> page = userRepository.findAll(spec, pageable);

        List<UUID> userIds = page.getContent().stream().map(User::getId).toList();
        Map<UUID, Long> urlCounts = resolveUrlCounts(userIds);

        return page.map(user -> AdminUserResponse.from(
                user,
                urlCounts.getOrDefault(user.getId(), 0L)
        ));
    }

    @Override
    @Transactional
    public AdminUserResponse updateUserStatus(UUID userId, String status, HttpServletRequest request) {
        User user = findActiveUser(userId);

        StatusType newStatus = parseAdminUserStatus(status);

        if (newStatus == StatusType.SUSPENDED && userId.equals(userService.getCurrentUser().getId())) {
            throw new ApplicationException(ExceptionType.BAD_REQUEST, "Tidak bisa suspend akun sendiri");
        }

        user.setStatus(newStatus);
        User updated = userRepository.save(user);

        cacheService.evict(CacheConstants.CACHE_AUTH + updated.getEmail());

        if (newStatus == StatusType.SUSPENDED) {
            refreshTokenService.revokeAllForUser(updated.getEmail());
        }

        ActionType action = newStatus == StatusType.SUSPENDED
                ? ActionType.SUSPEND_USER
                : ActionType.ACTIVATE_USER;
        auditLogService.saveAuditLog(request, action, TargetType.USER, updated.getId());

        return AdminUserResponse.from(updated, countUrlsForUser(updated.getId()));
    }

    @Override
    @Transactional
    public AdminUserResponse updateUserQuota(UUID userId, UpdateQuotaRequest quotaRequest, HttpServletRequest request) {
        User user = findActiveUser(userId);

        List<ApiKey> apiKeys = apiKeyRepository.findByUserId(userId).stream()
                .filter(key -> key.getStatus() == KeyStatusType.ACTIVE)
                .toList();
        if (apiKeys.isEmpty()) {
            throw new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND, "User has no active API keys");
        }

        for (ApiKey apiKey : apiKeys) {
            Quota quota = quotaRepository.findByApiKeyId(apiKey.getId())
                    .orElseThrow(() -> new ApplicationException(
                            ExceptionType.RESOURCE_NOT_FOUND,
                            "Quota not found for API key"));

            quota.setMaxRequestsPerDay(quotaRequest.getMaxRequestsPerDay());
            quota.setMaxUrlsPerKey(quotaRequest.getMaxUrlsPerKey());
            quota.setMaxBulk(quotaRequest.getMaxBulk());
            quotaRepository.save(quota);

            cacheService.evict(CacheConstants.CACHE_PLAN + apiKey.getKeyHash());
        }

        auditLogService.saveAuditLog(request, ActionType.UPDATE_QUOTA, TargetType.USER, user.getId());

        return AdminUserResponse.from(user, countUrlsForUser(user.getId()));
    }

    private long countUrlsForUser(UUID userId) {
        return urlRepository.countByUserId(userId);
    }

    private Map<UUID, Long> resolveUrlCounts(List<UUID> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }

        Map<UUID, Long> counts = new HashMap<>();
        urlRepository.countUrlsByUserIds(userIds).forEach(row -> {
            UUID userId = (UUID) row[0];
            Long count = ((Number) row[1]).longValue();
            counts.put(userId, count);
        });
        return counts;
    }

    private User findActiveUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ExceptionType.USER_NOT_FOUND));

        if (user.getDeletedAt() != null) {
            throw new ApplicationException(ExceptionType.USER_NOT_FOUND);
        }

        return user;
    }

    private StatusType parseAdminUserStatus(String status) {
        StatusType parsed = parseStatus(status);
        if (!ALLOWED_USER_STATUS_CHANGES.contains(parsed)) {
            throw new ApplicationException(
                    ExceptionType.VALIDATION_ERROR,
                    "Status hanya boleh ACTIVE atau SUSPENDED"
            );
        }
        return parsed;
    }

    private StatusType parseStatus(String status) {
        try {
            return StatusType.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApplicationException(ExceptionType.VALIDATION_ERROR, "Status tidak valid");
        }
    }

    private Pageable buildPageable(SearchUserRequest request) {
        int page = request.getPage() == null || request.getPage() < 1 ? 1 : request.getPage();
        int limit = request.getLimit() == null ? 10 : request.getLimit();
        if (limit < 1) {
            limit = 10;
        } else if (limit > 100) {
            limit = 100;
        }
        return PageRequest.of(page - 1, limit, buildSort(request.getSort()));
    }

    private Sort buildSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        String[] parts = sort.split(":");
        String field = switch (parts[0]) {
            case "email" -> "email";
            case "name" -> "name";
            case "created_at" -> "createdAt";
            default -> "createdAt";
        };

        Sort.Direction direction = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1]))
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return Sort.by(direction, field);
    }

    private Specification<User> buildUserSpecification(SearchUserRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (StringUtils.hasText(request.getSearch())) {
                String term = "%" + request.getSearch().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("email")), term),
                        cb.like(cb.lower(root.get("name")), term)
                ));
            }

            if (StringUtils.hasText(request.getStatus())) {
                try {
                    StatusType status = StatusType.valueOf(request.getStatus().toUpperCase());
                    predicates.add(cb.equal(root.get("status"), status));
                } catch (IllegalArgumentException ignored) {
                    // unknown filter value, ignore
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
