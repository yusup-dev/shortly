package com.shortly.apiservice.controller.admin;

import com.shortly.apiservice.dto.request.SearchUserRequest;
import com.shortly.apiservice.dto.request.UpdateQuotaRequest;
import com.shortly.apiservice.dto.request.UpdateStatusRequest;
import com.shortly.apiservice.dto.response.AdminUserResponse;
import com.shortly.apiservice.dto.response.ApiResponse;
import com.shortly.apiservice.dto.response.PaginationResponse;
import com.shortly.apiservice.service.AdminService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin User Controller")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminService adminService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminUserResponse>>> findAll(
            @ParameterObject @ModelAttribute SearchUserRequest request
    ) {
        Page<AdminUserResponse> page = adminService.findAllUsers(request);
        return ResponseEntity.ok(
                ApiResponse.<List<AdminUserResponse>>builder()
                        .success(true)
                        .message("Users retrieved successfully")
                        .data(page.getContent())
                        .pagination(PaginationResponse.fromPage(page))
                        .build()
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<AdminUserResponse>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStatusRequest updateStatusRequest,
            HttpServletRequest request
    ) {
        AdminUserResponse data = adminService.updateUserStatus(
                id, updateStatusRequest.getStatus(), request);
        return ResponseEntity.ok(
                ApiResponse.<AdminUserResponse>builder()
                        .success(true)
                        .message("User status updated successfully")
                        .data(data)
                        .build()
        );
    }

    @PatchMapping("/{id}/quota")
    public ResponseEntity<ApiResponse<AdminUserResponse>> updateQuota(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateQuotaRequest updateQuotaRequest,
            HttpServletRequest request
    ) {
        AdminUserResponse data = adminService.updateUserQuota(id, updateQuotaRequest, request);
        return ResponseEntity.ok(
                ApiResponse.<AdminUserResponse>builder()
                        .success(true)
                        .message("User quota updated successfully")
                        .data(data)
                        .build()
        );
    }
}
