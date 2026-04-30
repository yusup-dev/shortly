package com.shortly.apiservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyPlanCache {
    private Integer maxRequestsPerDay;
    private Integer maxUrlsPerKey;
}
