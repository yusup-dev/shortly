package com.shortly.apiservice.service.impl;

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
import com.shortly.apiservice.service.ApiKeyService;
import com.shortly.apiservice.service.CacheService;
import com.shortly.apiservice.utils.ApiKeyGenerator;
import com.shortly.apiservice.utils.ApiKeyHashUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

        ApiKey apiKey = buildApiKey(user, key.hash());
        ApiKey saved = apiKeyRepository.save(apiKey);

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

        ApiKey newKey = buildApiKey(oldKey.getUser(), key.hash());
        ApiKey saved = apiKeyRepository.save(newKey);

        updateQuota(oldKey, saved);

        return ApiKeyResponse.builder()
                .apiKey(key.raw())
                .build();
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

    public void createQuota(ApiKey apiKey, User user) {

        Quota quota = Quota.builder()
                .id(UUID.randomUUID())
                .apiKey(apiKey)
                .maxRequestsPerDay(user.getPlan().getMaxRequestsPerDay())
                .maxUrlsPerKey(user.getPlan().getMaxUrlsPerKey())
                .maxBulk(user.getPlan().getMaxBulk())
                .build();

        quotaRepository.save(quota);
    }

    @Override
    public void revokeApiKey(UUID apiKeyId) {
        ApiKey apiKey = apiKeyRepository.findById(apiKeyId)
                .orElseThrow(() -> new ApplicationException(
                        ExceptionType.RESOURCE_NOT_FOUND,
                        "Api Key not found"));
        revokeKey(apiKey);
    }

    @Override
    public List<ApiKeyListResponse> listByUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ExceptionType.USER_NOT_FOUND));

        return apiKeyRepository.findByUser(user).stream()
                .map(apiKey -> {
                    ApiKeyListResponse.ApiKeyListResponseBuilder builder = ApiKeyListResponse.builder()
                            .id(apiKey.getId())
                            .status(apiKey.getStatus() != null ? apiKey.getStatus().name() : null)
                            .expiresAt(apiKey.getExpiresAt())
                            .createdAt(apiKey.getCreatedAt());

                    quotaRepository.findByApiKeyId(apiKey.getId()).ifPresent(quota ->
                            builder.quota(ApiKeyListResponse.QuotaSummary.builder()
                                    .maxRequestsPerDay(quota.getMaxRequestsPerDay())
                                    .maxUrlsPerKey(quota.getMaxUrlsPerKey())
                                    .maxBulk(quota.getMaxBulk())
                                    .build())
                    );

                    return builder.build();
                })
                .toList();
    }

    @Override
    public void revokeOwnApiKey(UUID apiKeyId, UUID userId) {
        ApiKey apiKey = apiKeyRepository.findById(apiKeyId)
                .orElseThrow(() -> new ApplicationException(
                        ExceptionType.RESOURCE_NOT_FOUND,
                        "Api Key not found"));

        if (apiKey.getUser() == null || !apiKey.getUser().getId().equals(userId)) {
            throw new ApplicationException(ExceptionType.FORBIDDEN, "Api key ini bukan milik kamu");
        }

        revokeKey(apiKey);
    }

    private record GeneratedKey(String raw, String hash) {}
}
