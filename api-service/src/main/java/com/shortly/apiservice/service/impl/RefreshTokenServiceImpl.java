package com.shortly.apiservice.service.impl;

import com.shortly.apiservice.configuration.JwtSecretConfig;
import com.shortly.apiservice.constant.CacheConstants;
import com.shortly.apiservice.dto.UserInfo;
import com.shortly.apiservice.dto.response.AuthResponse;
import com.shortly.apiservice.dto.response.TokenResponse;
import com.shortly.apiservice.enumaration.ExceptionType;
import com.shortly.apiservice.exception.ApplicationException;
import com.shortly.apiservice.service.CacheService;
import com.shortly.apiservice.service.JwtService;
import com.shortly.apiservice.service.RefreshTokenService;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final CacheService cacheService;
    private final JwtService jwtService;
    private final JwtSecretConfig jwtSecretConfig;
    private final SecretKey signKey;
    private static final Duration REFRESH_TOKEN_EXPIRY = Duration.ofDays(7);

    @Override
    public String createRefreshToken(UserInfo userInfo) {
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + REFRESH_TOKEN_EXPIRY.toMillis());

        String refreshToken = Jwts.builder()
                .setSubject(userInfo.getUsername())
                .setIssuedAt(currentDate)
                .setExpiration(expireDate)
                .signWith(signKey)
                .compact();

        cacheService.put(CacheConstants.CACHE_REFRESH_TOKEN + refreshToken,
                userInfo.getUsername(),
                REFRESH_TOKEN_EXPIRY);

        return refreshToken;
    }

    @Override
    public AuthResponse refresh(String oldToken) {
        String username = cacheService.get(CacheConstants.CACHE_REFRESH_TOKEN + oldToken, String.class)
                .orElseThrow(() -> new ApplicationException(
                        ExceptionType.UNAUTHORIZED,
                        "Refresh token not valid or expired"
                ));
        cacheService.evict(CacheConstants.CACHE_REFRESH_TOKEN + oldToken);

        UserInfo userInfo = UserInfo.builder().email(username).build();
        TokenResponse newAccessToken = jwtService.generateToken(userInfo);
        String newRefreshToken = createRefreshToken(userInfo);

        return AuthResponse.fromRefresh(newAccessToken, newRefreshToken);
    }

    @Override
    public void deleteToken(String token) {
        cacheService.evict(CacheConstants.CACHE_REFRESH_TOKEN + token);
    }
}
