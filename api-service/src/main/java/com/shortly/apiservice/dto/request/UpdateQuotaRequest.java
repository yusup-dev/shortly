package com.shortly.apiservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateQuotaRequest {

    @NotNull
    @Min(0)
    private Integer maxRequestsPerDay;

    @NotNull
    @Min(0)
    private Integer maxUrlsPerKey;

    @NotNull
    @Min(0)
    private Integer maxBulk;
}
