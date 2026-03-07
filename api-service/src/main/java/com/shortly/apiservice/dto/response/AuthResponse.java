package com.shortly.apiservice.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.shortly.apiservice.dto.UserInfo;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AuthResponse {

    private String token;
    private Date expiresIn;
    private LoginResponse user;

    public static AuthResponse from(UserInfo userInfo, TokenResponse tokenResponse) {
        return  AuthResponse.builder()
                .token(tokenResponse.getToken())
                .expiresIn(tokenResponse.getExpiresIn())
                .user(LoginResponse.from(userInfo))
                .build();
    }
}
