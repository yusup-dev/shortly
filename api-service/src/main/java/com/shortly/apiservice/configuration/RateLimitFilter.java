package com.shortly.apiservice.configuration;

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
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return path.startsWith("/api/auth") ||
                path.startsWith("/api/v3/api-docs") ||
                path.startsWith("/api/swagger-ui") ||
                path.startsWith("/api/swagger-ui.html") ||
                path.startsWith("/api/webjars"); // redirect
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // =========================
        // GET API KEY
        // =========================
        String rawApiKey = request.getHeader("X-API-KEY");

        if (rawApiKey == null || rawApiKey.isBlank()) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.getWriter().write("Missing API KEY");
            return;
        }

        String hashedKey = ApiKeyHashUtil.hash(rawApiKey);

        try {
            // =========================
            // RATE LIMIT
            // =========================
            rateLimitService.checkRateLimit(hashedKey);

        } catch (ApplicationException ex) {

            if (ex.getType() == ExceptionType.TOO_MANY_REQUEST) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Too many requests");
            } else {
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                response.getWriter().write(ex.getMessage());
            }
            return;
        }

        filterChain.doFilter(request, response);
    }
}