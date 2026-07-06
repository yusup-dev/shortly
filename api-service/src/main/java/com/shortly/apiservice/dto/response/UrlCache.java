package com.shortly.apiservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
public class UrlCache {
    private UUID id;
    private String originalUrl;
    private String shortKey;
    private String status;
    private LocalDateTime expiresAt;
}
