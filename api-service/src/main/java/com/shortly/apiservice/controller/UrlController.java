package com.shortly.apiservice.controller;

import com.shortly.apiservice.dto.request.UrlRequest;
import com.shortly.apiservice.dto.response.ApiResponse;
import com.shortly.apiservice.dto.response.UrlResponse;
import com.shortly.apiservice.enumaration.ExceptionType;
import com.shortly.apiservice.exception.ApplicationException;
import com.shortly.apiservice.service.UrlService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Url Controller")
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class UrlController {

    private final UrlService urlService;

    @PostMapping("/urls")
    public ResponseEntity<ApiResponse<UrlResponse>> createUrl(
            @RequestBody UrlRequest urlRequest,
            @RequestHeader("X-API-KEY") String apiKey,
            HttpServletRequest request
    ) {
        try {
            UrlResponse data = urlService.createUrl(urlRequest, apiKey, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.<UrlResponse>builder()
                            .success(true)
                            .message("Create short url successfully!")
                            .data(data)
                            .build()
            );
        } catch (Exception e) {
            throw new ApplicationException(
                    ExceptionType.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }


    @GetMapping("/{short_key}")
    public void redirect(
            @PathVariable(name = "short_key") String short_key,
            HttpServletResponse response
    ) throws IOException {
        urlService.redirect(short_key, response);
    }
}
