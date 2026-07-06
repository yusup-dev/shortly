package com.shortly.apiservice.service;

public interface RateLimitService {
    void checkRateLimit(String apiKey);

    RateLimitStatus getStatus(String apiKey);

    record RateLimitStatus(long limit, long remaining, long resetEpochSeconds) {
    }
}
