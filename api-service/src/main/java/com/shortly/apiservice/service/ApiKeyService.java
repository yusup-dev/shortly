package com.shortly.apiservice.service;

import java.util.UUID;

public interface ApiKeyService {

    void createApiKey(UUID userId);
}
