package com.shortly.apiservice.service;

import java.util.UUID;

public interface ApiKeyService {

    String createApiKey(UUID userId);
}
