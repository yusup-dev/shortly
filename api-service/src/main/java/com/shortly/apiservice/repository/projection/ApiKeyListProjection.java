package com.shortly.apiservice.repository.projection;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ApiKeyListProjection {
    UUID getId();
    String getStatus();
    LocalDateTime getExpiresAt();
    LocalDateTime getCreatedAt();
    Integer getMaxRequestsPerDay();
    Integer getMaxUrlsPerKey();
    Integer getMaxBulk();
}
