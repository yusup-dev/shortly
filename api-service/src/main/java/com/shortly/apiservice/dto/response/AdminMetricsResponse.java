package com.shortly.apiservice.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AdminMetricsResponse {

    private UserMetrics users;
    private UrlMetrics urls;
    private ClickMetrics clicks;
    private ApiKeyMetrics apiKeys;

    @Data
    @Builder
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class UserMetrics {
        private long total;
        private long active;
        private long suspended;
        private long inactive;
    }

    @Data
    @Builder
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class UrlMetrics {
        private long total;
        private long active;
        private long expired;
        private long suspended;
    }

    @Data
    @Builder
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ClickMetrics {
        private long total;
        private long today;
    }

    @Data
    @Builder
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ApiKeyMetrics {
        private long total;
        private long active;
        private long inactive;
    }
}
