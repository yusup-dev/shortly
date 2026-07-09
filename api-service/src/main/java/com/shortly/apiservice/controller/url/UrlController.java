package com.shortly.apiservice.controller.url;

import com.shortly.apiservice.dto.request.BulkUrlRequest;
import com.shortly.apiservice.dto.request.SearchUrlRequest;
import com.shortly.apiservice.dto.request.UpdateExpiryRequest;
import com.shortly.apiservice.dto.request.UrlRequest;
import com.shortly.apiservice.dto.response.*;
import com.shortly.apiservice.constant.CacheConstants;
import com.shortly.apiservice.enumaration.ExceptionType;
import com.shortly.apiservice.exception.ApplicationException;
import com.shortly.apiservice.service.AnalyticsService;
import com.shortly.apiservice.service.CacheService;
import com.shortly.apiservice.service.QrCodeService;
import com.shortly.apiservice.service.UrlService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@Tag(name = "Url Controller")
@RequestMapping("/api/v1/urls")
public class UrlController {

    private final UrlService urlService;
    private final QrCodeService qrCodeService;
    private final AnalyticsService analyticsService;
    private final CacheService cacheService;

    @PostMapping
    public ResponseEntity<ApiResponse<UrlResponse>> create(
            @Valid @RequestBody UrlRequest urlRequest,
            @RequestHeader("X-API-KEY") String apiKey,
            HttpServletRequest request
    ) {
        UrlResponse data = urlService.createUrl(urlRequest, apiKey, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<UrlResponse>builder()
                        .success(true)
                        .message("Short URL created successfully")
                        .data(data)
                        .build()
        );
    }

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<BulkUrlResponse>> createBulk(
            @Valid @RequestBody BulkUrlRequest bulkUrlRequest,
            @RequestHeader("X-API-KEY") String apiKey,
            HttpServletRequest request
    ) {
        BulkUrlResponse data = urlService.createBulk(bulkUrlRequest, apiKey, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<BulkUrlResponse>builder()
                        .success(true)
                        .message("Bulk short URL processed")
                        .data(data)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UrlResponse>>> findAll(
            @ParameterObject @ModelAttribute SearchUrlRequest request
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
    public ResponseEntity<ApiResponse<UrlResponse>> findOne(@PathVariable UUID id) {
        UrlResponse data = urlService.findOne(id);
        return ResponseEntity.ok(
                ApiResponse.<UrlResponse>builder()
                        .success(true)
                        .message("Url retrieved successfully")
                        .data(data)
                        .build()
        );
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<UrlResponse>> updateExpiry(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateExpiryRequest updateExpiryRequest,
            HttpServletRequest request
    ) {
        UrlResponse data = urlService.updateExpiry(id, updateExpiryRequest, request);
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
    ) {
        urlService.delete(id, request);
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Url deleted successfully")
                        .build()
        );
    }

    private static final int QR_MIN_SIZE = 64;
    private static final int QR_MAX_SIZE = 1024;

    @GetMapping(value = "/{id}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> qr(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "300") int size
    ) {
        if (size < QR_MIN_SIZE || size > QR_MAX_SIZE) {
            throw new ApplicationException(
                    ExceptionType.VALIDATION_ERROR,
                    "Size harus antara " + QR_MIN_SIZE + " dan " + QR_MAX_SIZE
            );
        }

        UrlResponse url = urlService.findOne(id);
        String cacheKey = CacheConstants.CACHE_QR + id + ":" + size;

        byte[] data = cacheService.getBytes(cacheKey).orElseGet(() -> {
            byte[] image = qrCodeService.generate(url.getShortUrl(), url.getShortKey(), size);
            cacheService.putBytes(cacheKey, image, Duration.ofHours(24));
            return image;
        });

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + url.getShortKey() + ".png\"")
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .body(data);
    }

    @GetMapping("/{id}/analytics")
    public ResponseEntity<ApiResponse<ClickAnalyticsResponse>> analytics(
            @PathVariable UUID id,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to
    ) {
        UrlResponse url = urlService.findOne(id);

        LocalDate resolvedTo = to != null ? to : LocalDate.now();
        LocalDate resolvedFrom = from != null ? from : resolvedTo.minusDays(30);

        ClickAnalyticsResponse data = analyticsService.getAnalytics(id, url.getShortUrl(), resolvedFrom, resolvedTo);

        return ResponseEntity.ok(
                ApiResponse.<ClickAnalyticsResponse>builder()
                        .success(true)
                        .message("Analytics retrieved successfully")
                        .data(data)
                        .build()
        );
    }

    @GetMapping("/{id}/analytics/advanced")
    public ResponseEntity<ApiResponse<AdvancedAnalyticsResponse>> advancedAnalytics(
            @PathVariable UUID id,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to
    ) {
        // ownership check + ensures the URL exists
        urlService.findOne(id);

        LocalDate resolvedTo = to != null ? to : LocalDate.now();
        LocalDate resolvedFrom = from != null ? from : resolvedTo.minusDays(30);

        AdvancedAnalyticsResponse data = analyticsService.getAdvancedAnalytics(id, resolvedFrom, resolvedTo);

        return ResponseEntity.ok(
                ApiResponse.<AdvancedAnalyticsResponse>builder()
                        .success(true)
                        .message("Advanced analytics retrieved successfully")
                        .data(data)
                        .build()
        );
    }
}
