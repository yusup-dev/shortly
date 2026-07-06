package com.shortly.apiservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BulkUrlResultItem {
    private int index;
    private String status; // success | failed
    private String shortUrl;
    private BulkUrlError error;

    @Data
    @Builder
    public static class BulkUrlError {
        private String code;
        private String message;
    }
}
