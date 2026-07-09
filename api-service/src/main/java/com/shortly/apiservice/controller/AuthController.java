package com.shortly.apiservice.controller;

import com.shortly.apiservice.constant.CacheConstants;
import com.shortly.apiservice.dto.UserInfo;
import com.shortly.apiservice.dto.request.AuthRequest;
import com.shortly.apiservice.dto.request.RefreshTokenRequest;
import com.shortly.apiservice.dto.request.UserRegisterRequest;
import com.shortly.apiservice.dto.response.*;
import com.shortly.apiservice.entity.User;
import com.shortly.apiservice.service.AuthService;
import com.shortly.apiservice.service.CacheService;
import com.shortly.apiservice.service.JwtService;
import com.shortly.apiservice.service.RefreshTokenService;
import com.shortly.apiservice.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Date;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth Controller")
public class AuthController {

    private final JwtService jwtService;
    private final AuthService authService;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final CacheService cacheService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserRegisterResponse>> register(
            @Valid @RequestBody UserRegisterRequest userRegisterRequest
    ) {
        UserRegisterResponse data = userService.register(userRegisterRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<UserRegisterResponse>builder()
                        .success(true)
                        .message("Register successfully!")
                        .data(data)
                        .build()
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody AuthRequest authRequest
    ) {
        UserInfo userInfo = authService.authenticate(authRequest);
        TokenResponse tokenResponse = jwtService.generateToken(userInfo);
        String refreshToken = refreshTokenService.createRefreshToken(userInfo);
        return ResponseEntity.ok(
                ApiResponse.<AuthResponse>builder()
                        .success(true)
                        .message("Login successfully!")
                        .data(AuthResponse.from(userInfo, tokenResponse, refreshToken))
                        .build()
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me() {
        User currentUser = userService.getCurrentUser();
        return ResponseEntity.ok(
                ApiResponse.<UserResponse>builder()
                        .success(true)
                        .message("User profile retrieved successfully!")
                        .data(UserResponse.from(currentUser))
                        .build()
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        AuthResponse data = refreshTokenService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(
                ApiResponse.<AuthResponse>builder()
                        .success(true)
                        .message("Token refreshed successfully!")
                        .data(data)
                        .build()
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestBody(required = false) RefreshTokenRequest request,
            HttpServletRequest httpRequest
    ) {
        blacklistAccessToken(httpRequest);

        if (request != null && request.getRefreshToken() != null) {
            refreshTokenService.deleteToken(request.getRefreshToken());
        }

        return ResponseEntity.noContent().build();
    }

    private void blacklistAccessToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer == null || !bearer.startsWith("Bearer ")) {
            return;
        }

        String accessToken = bearer.substring(7);
        try {
            Date expiration = jwtService.getExpiration(accessToken);
            long remainingMs = expiration.getTime() - System.currentTimeMillis();
            if (remainingMs > 0) {
                cacheService.put(
                        CacheConstants.CACHE_TOKEN_BLACKLIST + accessToken,
                        "true",
                        Duration.ofMillis(remainingMs)
                );
            }
        } catch (Exception e) {
            log.warn("Failed to blacklist access token on logout: {}", e.getMessage());
        }
    }
}
