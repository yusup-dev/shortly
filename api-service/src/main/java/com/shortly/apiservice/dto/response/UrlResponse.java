package com.shortly.apiservice.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.shortly.apiservice.entity.Url;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UrlResponse {
    private UUID id;
    private String originalUrl;
    private String shortedUrl;

    public static UrlResponse from(Url url, String baseUrl) {
        return UrlResponse.builder()
                .id(url.getId())
                .originalUrl(url.getOriginalUrl())
                .shortedUrl(baseUrl + "/" + url.getShortKey())
                .build();
    }
}
