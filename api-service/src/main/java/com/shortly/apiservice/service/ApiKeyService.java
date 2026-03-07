package com.shortly.apiservice.service;

import com.shortly.apiservice.dto.response.ApiKeyResponse;
import com.shortly.apiservice.entity.ApiKey;
import com.shortly.apiservice.entity.User;

import java.util.UUID;

public interface ApiKeyService {

    String createApiKey(UUID userId);
    ApiKeyResponse updateApiKey(UUID apiKey);
    void revokeApiKey(UUID apoKey);
}
