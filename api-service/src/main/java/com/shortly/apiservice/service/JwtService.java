package com.shortly.apiservice.service;

import com.shortly.apiservice.dto.UserInfo;
import com.shortly.apiservice.dto.response.TokenResponse;

public interface JwtService {
    TokenResponse generateToken(UserInfo userInfo);
    boolean validateToken(String token);
    String getUsernameFromToken(String token);
}
