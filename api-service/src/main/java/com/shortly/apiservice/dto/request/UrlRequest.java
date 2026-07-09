package com.shortly.apiservice.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UrlRequest {
    @NotBlank(message = "original_url wajib diisi")
    private String originalUrl;

    /** Custom short key, Pro plan only. */
    private String alias;

    /** Custom expiry date, Pro plan only. */
    private LocalDateTime expireAt;
}
