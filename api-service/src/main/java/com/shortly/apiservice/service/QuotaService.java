package com.shortly.apiservice.service;

public interface QuotaService {
    void checkQuota(String apiKey);
    void incrementQuota(String apiKey);
    void decrementQuota(String apiKey);
}
