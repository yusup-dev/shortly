package com.shortly.apiservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Builder
@Data
public class UrlCache {
    private UUID id;
    private String originalUrl;
    private String shortKey;
}
