package com.shortly.apiservice.service;

import com.shortly.apiservice.dto.response.ApiKeyListResponse;
import com.shortly.apiservice.dto.response.ApiKeyResponse;

import java.util.List;
import java.util.UUID;

public interface ApiKeyService {

    String createApiKey(UUID userId);
    ApiKeyResponse updateApiKey(UUID apiKey);
    void revokeApiKey(UUID apiKey);

    List<ApiKeyListResponse> listByUser(UUID userId);
    void revokeOwnApiKey(UUID apiKeyId, UUID userId);
}
