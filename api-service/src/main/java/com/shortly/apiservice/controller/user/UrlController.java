package com.shortly.apiservice.controller.user;

import com.shortly.apiservice.dto.request.UpdateUrlRequest;
import com.shortly.apiservice.dto.request.UrlRequest;
import com.shortly.apiservice.dto.request.SearchUrlRequest;
import com.shortly.apiservice.dto.response.ApiResponse;
import com.shortly.apiservice.dto.response.PaginationResponse;
import com.shortly.apiservice.dto.response.UrlResponse;
import com.shortly.apiservice.service.UrlService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Url Controller")
@RequestMapping("/api/urls")
public class UrlController {

    private final UrlService urlService;

    @PostMapping
    public ResponseEntity<ApiResponse<UrlResponse>> createUrl(
            @RequestBody UrlRequest urlRequest,
            @RequestHeader("X-API-KEY") String apiKey,
            HttpServletRequest request
    ) {
        UrlResponse data = urlService.createUrl(urlRequest, apiKey, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<UrlResponse>builder()
                        .success(true)
                        .message("Create short url successfully!")
                        .data(data)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UrlResponse>>> findAll(
            @ParameterObject
            @ModelAttribute SearchUrlRequest request
    ) {
        Page<UrlResponse> page = urlService.findAll(request);
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
        UrlResponse data = urlService.findOne(id);
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
            @RequestBody UpdateUrlRequest updateUrlRequest,
            HttpServletRequest request

    ) {
        UrlResponse data = urlService.update(id, updateUrlRequest, request);
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
        urlService.delete(id, request);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Url deleted successfully")
                        .build()
        );
    }
}
