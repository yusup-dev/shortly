package com.shortly.apiservice.service;

import com.shortly.apiservice.dto.response.AdvancedAnalyticsResponse;
import com.shortly.apiservice.dto.response.ClickAnalyticsResponse;

import java.time.LocalDate;
import java.util.UUID;

public interface AnalyticsService {

    /**
     * Records a single redirect click asynchronously (parses user-agent, resolves
     * GeoIP, persists the raw event and updates Redis counters/HyperLogLog).
     * Must never throw back to the caller - failures are logged only.
     */
    void recordClickAsync(UUID urlId, String ipAddress, String userAgent, String referer);

    long getTotalClicks(UUID urlId);

    ClickAnalyticsResponse getAnalytics(UUID urlId, String shortUrl, LocalDate from, LocalDate to);

    AdvancedAnalyticsResponse getAdvancedAnalytics(UUID urlId, LocalDate from, LocalDate to);
}
