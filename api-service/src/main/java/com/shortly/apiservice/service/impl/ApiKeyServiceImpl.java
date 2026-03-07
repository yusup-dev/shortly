package com.shortly.apiservice.service.impl;

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
import com.shortly.apiservice.utils.ApiKeyGenerator;
import com.shortly.apiservice.utils.ApiKeyHashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApiKeyServiceImpl implements ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final UserRepository userRepository;
    private final QuotaRepository quotaRepository;

    @Override
    public String createApiKey(UUID userId) {
        String rawKey = ApiKeyGenerator.generateApiKey();
        String hash = ApiKeyHashUtil.hash(rawKey);

        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApplicationException(ExceptionType.USER_NOT_FOUND)
        );

        ApiKey apiKey = ApiKey.builder()
                .id(UUID.randomUUID())
                .user(user)
                .keyHash(hash)
                .expiresAt(null)
                .status(KeyStatusType.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
        ApiKey saved = apiKeyRepository.save(apiKey);

        Quota quota = Quota.builder()
                .id(UUID.randomUUID())
                .apiKey(saved)
                .maxRequestsPerDay(user.getPlan().getMaxRequestsPerDay())
                .maxUrlsPerKey(user.getPlan().getMaxUrlsPerKey())
                .maxBulk(user.getPlan().getMaxBulk())
                .build();
        quotaRepository.save(quota);

        return rawKey;
    }
}
