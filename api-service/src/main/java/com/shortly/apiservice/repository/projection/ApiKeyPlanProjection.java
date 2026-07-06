package com.shortly.apiservice.repository.projection;

public interface ApiKeyPlanProjection {
    String getKeyHash();
    Integer getMaxRequestsPerDay();
    Integer getMaxUrlsPerKey();
    Integer getMaxBulk();
}
