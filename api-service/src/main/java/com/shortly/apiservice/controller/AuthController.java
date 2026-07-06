package com.shortly.apiservice.controller;

import com.shortly.apiservice.constant.CacheConstants;
import com.shortly.apiservice.dto.UserInfo;
import com.shortly.apiservice.dto.request.AuthRequest;
import com.shortly.apiservice.dto.request.UserRegisterRequest;
import com.shortly.apiservice.dto.response.*;
import com.shortly.apiservice.entity.User;
import com.shortly.apiservice.enumaration.ExceptionType;
import com.shortly.apiservice.exception.ApplicationException;
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
import java.util.Map;

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

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody AuthRequest authRequest
    ) {
        try {
            UserInfo userInfo = authService.authenticate(authRequest);
            TokenResponse tokenResponse = jwtService.generateToken(userInfo);
            String refreshToken = refreshTokenService.createRefreshToken(userInfo);
            return ResponseEntity.status(HttpStatus.OK).body(
                    ApiResponse.<AuthResponse>builder()
                            .success(true)
                            .message("Login successfully!")
                            .data(AuthResponse.from(userInfo, tokenResponse, refreshToken))
                            .build()
            );
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ApplicationException(ExceptionType.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserRegisterResponse>> login(
            @Valid @RequestBody UserRegisterRequest userRegisterRequest
            ) {
        try {
            UserRegisterResponse data = userService.register(userRegisterRequest);
            return ResponseEntity.status(HttpStatus.OK).body(
                    ApiResponse.<UserRegisterResponse>builder()
                            .success(true)
                            .message("Register successfully!")
                            .data(data)
                            .build()
            );
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ApplicationException(ExceptionType.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String, String> body) {
        try {
          return ResponseEntity.status(HttpStatus.OK)
                    .body(refreshTokenService.refresh(body.get("refreshToken")));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ApplicationException(ExceptionType.UNAUTHORIZED, e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestBody(required = false) Map<String, String> body,
            HttpServletRequest request
    ) {
        blacklistAccessToken(request);

        if (body != null && body.get("refreshToken") != null) {
            refreshTokenService.deleteToken(body.get("refreshToken"));
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

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me() {
        try {
            User currentUser = userService.getCurrentUser();

            return ResponseEntity.ok(
                    ApiResponse.<UserResponse>builder()
                            .success(true)
                            .message("User profile retrieved successfully!")
                            .data(UserResponse.from(currentUser))
                            .build()
            );
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ApplicationException(
                    ExceptionType.UNAUTHORIZED,
                    e.getMessage()
            );
        }
    }
}
