package com.shortly.apiservice.controller.url;

import com.shortly.apiservice.dto.request.SearchUrlRequest;
import com.shortly.apiservice.dto.request.UpdateStatusRequest;
import com.shortly.apiservice.dto.request.UpdateUrlRequest;
import com.shortly.apiservice.dto.response.ApiResponse;
import com.shortly.apiservice.dto.response.PaginationResponse;
import com.shortly.apiservice.dto.response.UrlResponse;
import com.shortly.apiservice.service.UrlService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@Tag(name = "Admin Url Controller")
@RequestMapping("/api/v1/admin/urls")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUrlController {

    private final UrlService urlService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UrlResponse>>> findAll(
            @ParameterObject
            @ModelAttribute SearchUrlRequest request
    ) {
        Page<UrlResponse> page = urlService.findAllForAdmin(request);
        return ResponseEntity.ok(
                ApiResponse.<List<UrlResponse>>builder()
                        .success(true)
                        .message("Urls retrieved successfully")
                        .data(page.getContent())
                        .pagination(PaginationResponse.fromPage(page))
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UrlResponse>> findOne(
            @PathVariable UUID id
    ) {
        UrlResponse data = urlService.findOneForAdmin(id);
        return ResponseEntity.ok(
                ApiResponse.<UrlResponse>builder()
                        .success(true)
                        .message("Url retrieved successfully")
                        .data(data)
                        .build()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UrlResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUrlRequest updateUrlRequest,
            HttpServletRequest request
    ) {
        UrlResponse data = urlService.updateForAdmin(id, updateUrlRequest, request);
        return ResponseEntity.ok(
                ApiResponse.<UrlResponse>builder()
                        .success(true)
                        .message("Url updated successfully")
                        .data(data)
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> delete(
            @PathVariable UUID id,
            HttpServletRequest request
    ){
        urlService.deleteForAdmin(id, request);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Url deleted successfully")
                        .build()
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UrlResponse>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStatusRequest updateStatusRequest,
            HttpServletRequest request
    ) {
        UrlResponse data = urlService.updateStatusForAdmin(
                id, updateStatusRequest.getStatus(), updateStatusRequest.getReason(), request);
        return ResponseEntity.ok(
                ApiResponse.<UrlResponse>builder()
                        .success(true)
                        .message("Url status updated successfully")
                        .data(data)
                        .build()
        );
    }
}
