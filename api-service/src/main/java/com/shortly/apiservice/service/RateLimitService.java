package com.shortly.apiservice.service;

import org.springframework.stereotype.Service;

public interface RateLimitService {
    void checkRateLimit(String apiKey);
}
