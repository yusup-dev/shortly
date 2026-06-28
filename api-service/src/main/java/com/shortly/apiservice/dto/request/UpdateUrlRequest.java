package com.shortly.apiservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateUrlRequest {

    @NotBlank
    private String originalUrl;
}
