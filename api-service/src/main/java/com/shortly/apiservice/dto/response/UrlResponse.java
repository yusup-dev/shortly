package com.shortly.apiservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.shortly.apiservice.entity.Url;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UrlResponse {
    private UUID id;
    private String shortKey;
    private String shortUrl;
    private String originalUrl;
    private String status;
    private Long totalClicks;
    private LocalDateTime expireAt;
    private LocalDateTime createdAt;
    private String qrUrl;

    public static UrlResponse from(Url url, String baseUrl) {
        return UrlResponse.builder()
                .id(url.getId())
                .shortKey(url.getShortKey())
                .shortUrl(baseUrl + "/" + url.getShortKey())
                .originalUrl(url.getOriginalUrl())
                .status(url.getStatus() != null ? url.getStatus().name() : null)
                .expireAt(url.getExpiresAt())
                .createdAt(url.getCreatedAt())
                .qrUrl(baseUrl + "/api/v1/urls/" + url.getId() + "/qr")
                .build();
    }

    public static UrlResponse from(Url url, String baseUrl, Long totalClicks) {
        UrlResponse response = from(url, baseUrl);
        response.setTotalClicks(totalClicks);
        return response;
    }
}
