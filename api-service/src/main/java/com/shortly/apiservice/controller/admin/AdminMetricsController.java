package com.shortly.apiservice.controller.admin;

import com.shortly.apiservice.dto.response.AdminMetricsResponse;
import com.shortly.apiservice.dto.response.ApiResponse;
import com.shortly.apiservice.service.AdminService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/metrics")
@RequiredArgsConstructor
@Tag(name = "Admin Metrics Controller")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMetricsController {

    private final AdminService adminService;

    @GetMapping
    public ResponseEntity<ApiResponse<AdminMetricsResponse>> getMetrics() {
        AdminMetricsResponse data = adminService.getMetrics();
        return ResponseEntity.ok(
                ApiResponse.<AdminMetricsResponse>builder()
                        .success(true)
                        .message("System metrics retrieved successfully")
                        .data(data)
                        .build()
        );
    }
}
