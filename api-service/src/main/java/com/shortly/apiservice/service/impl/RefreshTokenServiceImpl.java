package com.shortly.apiservice.service.impl;

import com.shortly.apiservice.constant.CacheConstants;
import com.shortly.apiservice.dto.UserInfo;
import com.shortly.apiservice.dto.response.AuthResponse;
import com.shortly.apiservice.dto.response.TokenResponse;
import com.shortly.apiservice.enumaration.ExceptionType;
import com.shortly.apiservice.enumaration.StatusType;
import com.shortly.apiservice.exception.ApplicationException;
import com.shortly.apiservice.repository.UserRepository;
import com.shortly.apiservice.repository.projection.UserAuthProjection;
import com.shortly.apiservice.service.CacheService;
import com.shortly.apiservice.service.JwtService;
import com.shortly.apiservice.service.RefreshTokenService;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final CacheService cacheService;
    private final JwtService jwtService;
    private final SecretKey signKey;
    private final UserRepository userRepository;
    private final StringRedisTemplate stringRedisTemplate;

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

        trackRefreshToken(userInfo.getUsername(), refreshToken);

        return refreshToken;
    }

    @Override
    public AuthResponse refresh(String oldToken) {
        String username = cacheService.get(CacheConstants.CACHE_REFRESH_TOKEN + oldToken, String.class)
                .orElseThrow(() -> new ApplicationException(
                        ExceptionType.UNAUTHORIZED,
                        "Refresh token not valid or expired"
                ));

        UserAuthProjection auth = userRepository.findAuthByEmail(username)
                .orElseThrow(() -> new ApplicationException(ExceptionType.USER_NOT_FOUND));

        if (StatusType.SUSPENDED.name().equals(auth.getStatus())) {
            deleteToken(oldToken);
            throw new ApplicationException(ExceptionType.ACCOUNT_SUSPENDED);
        }

        cacheService.evict(CacheConstants.CACHE_REFRESH_TOKEN + oldToken);
        untrackRefreshToken(username, oldToken);

        UserInfo userInfo = UserInfo.builder()
                .id(auth.getId())
                .email(auth.getEmail())
                .role(auth.getRoleName())
                .status(auth.getStatus())
                .build();

        TokenResponse newAccessToken = jwtService.generateToken(userInfo);
        String newRefreshToken = createRefreshToken(userInfo);

        return AuthResponse.fromRefresh(newAccessToken, newRefreshToken);
    }

    @Override
    public void deleteToken(String token) {
        String username = cacheService.get(CacheConstants.CACHE_REFRESH_TOKEN + token, String.class)
                .orElse(null);
        cacheService.evict(CacheConstants.CACHE_REFRESH_TOKEN + token);
        if (username != null) {
            untrackRefreshToken(username, token);
        }
    }

    @Override
    public void revokeAllForUser(String email) {
        String setKey = userRefreshTokensKey(email);
        Set<String> tokens = stringRedisTemplate.opsForSet().members(setKey);
        if (tokens != null) {
            for (String token : tokens) {
                cacheService.evict(CacheConstants.CACHE_REFRESH_TOKEN + token);
            }
        }
        stringRedisTemplate.delete(setKey);
    }

    private void trackRefreshToken(String email, String token) {
        String setKey = userRefreshTokensKey(email);
        stringRedisTemplate.opsForSet().add(setKey, token);
        stringRedisTemplate.expire(setKey, REFRESH_TOKEN_EXPIRY);
    }

    private void untrackRefreshToken(String email, String token) {
        stringRedisTemplate.opsForSet().remove(userRefreshTokensKey(email), token);
    }

    private String userRefreshTokensKey(String email) {
        return CacheConstants.CACHE_USER_REFRESH_TOKENS + email;
    }
}
