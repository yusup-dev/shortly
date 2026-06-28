package com.shortly.apiservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.shortly.apiservice.dto.UserInfo;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AuthResponse {

    private String accessToken;
    private Date accessTokenExpireIn;
    private String refreshToken;
    private LoginResponse user;

    public static AuthResponse from(UserInfo userInfo, TokenResponse tokenResponse, String refreshToken) {
        return  AuthResponse.builder()
                .user(LoginResponse.from(userInfo))
                .accessToken(tokenResponse.getToken())
                .accessTokenExpireIn(tokenResponse.getExpiresIn())
                .refreshToken(refreshToken)
                .build();
    }

    public static AuthResponse fromRefresh(TokenResponse tokenResponse, String refreshToken) {
        return  AuthResponse.builder()
                .accessToken(tokenResponse.getToken())
                .accessTokenExpireIn(tokenResponse.getExpiresIn())
                .refreshToken(refreshToken)
                .build();
    }
}
