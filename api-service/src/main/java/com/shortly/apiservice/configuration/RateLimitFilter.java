package com.shortly.apiservice.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shortly.apiservice.dto.response.ErrorResponse;
import com.shortly.apiservice.enumaration.ExceptionType;
import com.shortly.apiservice.exception.ApplicationException;
import com.shortly.apiservice.service.RateLimitService;
import com.shortly.apiservice.utils.ApiKeyHashUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getRequestURI();
        String method = request.getMethod();

        boolean protectedEndpoint =
                "POST".equalsIgnoreCase(method)
                        && (
                        "/api/v1/urls".equals(path)
                                || "/api/v1/urls/bulk".equals(path)
                );

        return !protectedEndpoint;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String rawApiKey = request.getHeader("X-API-KEY");

        if (rawApiKey == null || rawApiKey.isBlank()) {

            writeErrorResponse(
                    response,
                    HttpStatus.BAD_REQUEST,
                    "MISSING_API_KEY",
                    "Missing API KEY"
            );

            return;
        }

        String hashedKey = ApiKeyHashUtil.hash(rawApiKey);

        try {

            rateLimitService.checkRateLimit(hashedKey);

            addRateLimitHeaders(response, hashedKey);

            filterChain.doFilter(request, response);

        } catch (ApplicationException ex) {

            addRateLimitHeaders(response, hashedKey);

            HttpStatus status = HttpStatus.resolve(ex.getType().getHttpCode());
            if (status == null) {
                status = HttpStatus.BAD_REQUEST;
            }

            writeErrorResponse(
                    response,
                    status,
                    ex.getType().name(),
                    ex.getMessage()
            );
        }
    }

    private void addRateLimitHeaders(HttpServletResponse response, String hashedKey) {
        try {
            RateLimitService.RateLimitStatus status = rateLimitService.getStatus(hashedKey);
            response.setHeader("X-RateLimit-Limit", String.valueOf(status.limit()));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(status.remaining()));
            response.setHeader("X-RateLimit-Reset", String.valueOf(status.resetEpochSeconds()));
        } catch (Exception ignored) {
            // best-effort headers only, never block the request because of them
        }
    }

    private void writeErrorResponse(
            HttpServletResponse response,
            HttpStatus status,
            String code,
            String message
    ) throws IOException {

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .error(ErrorResponse.ErrorDetail.builder()
                        .code(code)
                        .message(message)
                        .build())
                .timestamp(LocalDateTime.now())
                .build();

        objectMapper.writeValue(
                response.getWriter(),
                errorResponse
        );
    }
}
