package com.shortly.apiservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiKeyListResponse {
    private UUID id;
    private String status;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private QuotaSummary quota;

    @Data
    @Builder
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class QuotaSummary {
        private Integer maxRequestsPerDay;
        private Integer maxUrlsPerKey;
        private Integer maxBulk;
    }
}
