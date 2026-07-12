package com.shortly.apiservice.service;

import com.shortly.apiservice.dto.request.SearchUserRequest;
import com.shortly.apiservice.dto.request.UpdateQuotaRequest;
import com.shortly.apiservice.dto.response.AdminMetricsResponse;
import com.shortly.apiservice.dto.response.AdminUserResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface AdminService {

    AdminMetricsResponse getMetrics();

    Page<AdminUserResponse> findAllUsers(SearchUserRequest request);

    AdminUserResponse updateUserStatus(UUID userId, String status, HttpServletRequest request);

    AdminUserResponse updateUserQuota(UUID userId, UpdateQuotaRequest quotaRequest, HttpServletRequest request);
}
