package com.shortly.apiservice.service;

import com.shortly.apiservice.dto.response.ApiKeyPlanCache;

public interface PlanService {
    ApiKeyPlanCache getPlan(String apiKey);
}
