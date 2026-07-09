package com.shortly.apiservice.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AdvancedAnalyticsResponse {
    private PeriodResponse period;
    private long totalClicks;
    private long uniqueVisitors;
    private List<Map<String, Object>> byDay;
    private List<Map<String, Object>> byCountry;
    private List<Map<String, Object>> byDevice;
    private List<Map<String, Object>> byOs;
    private List<Map<String, Object>> byBrowser;
    private List<Map<String, Object>> byReferrer;
}
