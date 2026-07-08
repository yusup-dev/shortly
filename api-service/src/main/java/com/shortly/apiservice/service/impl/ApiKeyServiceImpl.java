package com.shortly.apiservice.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.shortly.apiservice.constant.CacheConstants;
import com.shortly.apiservice.dto.response.ApiKeyListResponse;
import com.shortly.apiservice.dto.response.ApiKeyResponse;
import com.shortly.apiservice.entity.ApiKey;
import com.shortly.apiservice.entity.Quota;
import com.shortly.apiservice.entity.User;
import com.shortly.apiservice.enumaration.ExceptionType;
import com.shortly.apiservice.enumaration.KeyStatusType;
import com.shortly.apiservice.exception.ApplicationException;
import com.shortly.apiservice.repository.ApiKeyRepository;
import com.shortly.apiservice.repository.QuotaRepository;
import com.shortly.apiservice.repository.UserRepository;
import com.shortly.apiservice.repository.projection.ApiKeyListProjection;
import com.shortly.apiservice.service.ApiKeyService;
import com.shortly.apiservice.service.CacheService;
import com.shortly.apiservice.utils.ApiKeyGenerator;
import com.shortly.apiservice.utils.ApiKeyHashUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiKeyServiceImpl implements ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final UserRepository userRepository;
    private final QuotaRepository quotaRepository;
    private final CacheService cacheService;

    @Override
    @Transactional
    public String createApiKey(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ExceptionType.USER_NOT_FOUND));

        GeneratedKey key = generateKey();
        ApiKey saved = apiKeyRepository.save(buildApiKey(user, key.hash()));
        createQuota(saved, user);

        return key.raw();
    }

    @Override
    @Transactional
    public ApiKeyResponse updateApiKey(UUID apiKeyId) {
        ApiKey oldKey = apiKeyRepository.findById(apiKeyId)
                .orElseThrow(() -> new ApplicationException(
                        ExceptionType.RESOURCE_NOT_FOUND,
                        "Api Key not found"));

        revokeKey(oldKey);

        GeneratedKey key = generateKey();
        ApiKey saved = apiKeyRepository.save(buildApiKey(oldKey.getUser(), key.hash()));
        updateQuota(oldKey, saved);

        return ApiKeyResponse.builder()
                .apiKey(key.raw())
                .warning("Save this API key now, we won't show it again!")
                .build();
    }

    @Override
    @Transactional
    public void revokeApiKey(UUID apiKeyId) {
        ApiKey apiKey = apiKeyRepository.findById(apiKeyId)
                .orElseThrow(() -> new ApplicationException(
                        ExceptionType.RESOURCE_NOT_FOUND,
                        "Api Key not found"));
        revokeKey(apiKey);
    }

    @Override
    public List<ApiKeyListResponse> listByUser(UUID userId) {
        return apiKeyRepository.findListByUserId(userId).stream()
                .map(this::toListResponse)
                .toList();
    }

    @Override
    @Transactional
    public void revokeOwnApiKey(UUID apiKeyId, UUID userId) {
        apiKeyRepository.findByIdAndUser_Id(apiKeyId, userId)
                .ifPresentOrElse(
                        this::revokeKey,
                        () -> {
                            if (apiKeyRepository.existsById(apiKeyId)) {
                                throw new ApplicationException(ExceptionType.FORBIDDEN, "This api key is not yours");
                            }
                            throw new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND, "Api Key not found");
                        }
                );
    }

    private ApiKeyListResponse toListResponse(ApiKeyListProjection projection) {
        ApiKeyListResponse.ApiKeyListResponseBuilder builder = ApiKeyListResponse.builder()
                .id(projection.getId())
                .status(projection.getStatus())
                .expiresAt(projection.getExpiresAt())
                .createdAt(projection.getCreatedAt());

        if (projection.getMaxRequestsPerDay() != null) {
            builder.quota(ApiKeyListResponse.QuotaSummary.builder()
                    .maxRequestsPerDay(projection.getMaxRequestsPerDay())
                    .maxUrlsPerKey(projection.getMaxUrlsPerKey())
                    .maxBulk(projection.getMaxBulk())
                    .build());
        }

        return builder.build();
    }

    private GeneratedKey generateKey() {
        String raw = ApiKeyGenerator.generateApiKey();
        String hash = ApiKeyHashUtil.hash(raw);
        return new GeneratedKey(raw, hash);
    }

    private ApiKey buildApiKey(User user, String hash) {
        return ApiKey.builder()
                .id(UUID.randomUUID())
                .user(user)
                .keyHash(hash)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .status(KeyStatusType.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private void revokeKey(ApiKey apiKey) {
        if (apiKey.getStatus() == KeyStatusType.REVOKED) {
            return;
        }
        apiKey.setStatus(KeyStatusType.REVOKED);
        apiKeyRepository.save(apiKey);
        cacheService.evict(CacheConstants.CACHE_PLAN + apiKey.getKeyHash());
    }

    private void updateQuota(ApiKey oldKey, ApiKey newKey) {
        Quota quota = quotaRepository.findByApiKeyId(oldKey.getId())
                .orElseThrow(() -> new ApplicationException(
                        ExceptionType.RESOURCE_NOT_FOUND,
                        "Quota not found"));

        quota.setApiKey(newKey);
        quotaRepository.save(quota);
    }

    private void createQuota(ApiKey apiKey, User user) {
        Quota quota = Quota.builder()
                .id(UUID.randomUUID())
                .apiKey(apiKey)
                .maxRequestsPerDay(user.getPlan().getMaxRequestsPerDay())
                .maxUrlsPerKey(user.getPlan().getMaxUrlsPerKey())
                .maxBulk(user.getPlan().getMaxBulk())
                .build();

        quotaRepository.save(quota);
    }

    private record GeneratedKey(String raw, String hash) {}
}
