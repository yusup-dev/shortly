package com.shortly.apiservice.controller;

import com.shortly.apiservice.dto.UserInfo;
import com.shortly.apiservice.dto.request.AuthRequest;
import com.shortly.apiservice.dto.response.ApiResponse;
import com.shortly.apiservice.dto.response.AuthResponse;
import com.shortly.apiservice.dto.response.TokenResponse;
import com.shortly.apiservice.enumaration.ExceptionType;
import com.shortly.apiservice.exception.ApplicationException;
import com.shortly.apiservice.service.AuthService;
import com.shortly.apiservice.service.JwtService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth Controller")
public class AuthController {

    private final JwtService jwtService;
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody AuthRequest authRequest
    ) {
        try {
            UserInfo userInfo = authService.authenticate(authRequest);
            TokenResponse tokenResponse = jwtService.generateToken(userInfo);
            return ResponseEntity.status(HttpStatus.OK).body(
                    ApiResponse.<AuthResponse>builder()
                            .success(true)
                            .message("Login successfully!")
                            .data(AuthResponse.from(userInfo, tokenResponse))
                            .build()
            );
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ApplicationException(ExceptionType.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

}
