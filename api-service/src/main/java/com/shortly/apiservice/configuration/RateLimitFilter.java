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
                        "/api/urls".equals(path)
                                || "/api/urls/bulk".equals(path)
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
                    "Missing API KEY"
            );

            return;
        }

        String hashedKey = ApiKeyHashUtil.hash(rawApiKey);

        try {

            rateLimitService.checkRateLimit(hashedKey);

            filterChain.doFilter(request, response);

        } catch (ApplicationException ex) {

            HttpStatus status =
                    ex.getType() == ExceptionType.TOO_MANY_REQUEST
                            ? HttpStatus.TOO_MANY_REQUESTS
                            : HttpStatus.BAD_REQUEST;

            writeErrorResponse(
                    response,
                    status,
                    ex.getMessage()
            );
        }
    }

    private void writeErrorResponse(
            HttpServletResponse response,
            HttpStatus status,
            String message
    ) throws IOException {

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(status.value())
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        objectMapper.writeValue(
                response.getWriter(),
                errorResponse
        );
    }
}
