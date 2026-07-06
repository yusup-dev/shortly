package com.shortly.apiservice.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BulkUrlItemRequest {
    private String originalUrl;
    private String alias;
    private LocalDateTime expireAt;
}
