package com.shortly.apiservice.service;

import com.shortly.apiservice.dto.UserInfo;
import com.shortly.apiservice.dto.response.AuthResponse;

public interface RefreshTokenService {
    String createRefreshToken(UserInfo userInfo);
    AuthResponse refresh(String oldToken);
    void deleteToken(String token);
}
