package com.shortly.apiservice.service.impl;

import com.shortly.apiservice.constant.CacheConstants;
import com.shortly.apiservice.dto.response.TokenResponse;
import com.shortly.apiservice.enumaration.ExceptionType;
import com.shortly.apiservice.enumaration.StatusType;
import com.shortly.apiservice.exception.ApplicationException;
import com.shortly.apiservice.repository.UserRepository;
import com.shortly.apiservice.repository.projection.UserAuthProjection;
import com.shortly.apiservice.service.CacheService;
import com.shortly.apiservice.service.JwtService;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock private CacheService cacheService;
    @Mock private JwtService jwtService;
    @Mock private UserRepository userRepository;
    @Mock private StringRedisTemplate stringRedisTemplate;

    private SecretKey signKey;

    private RefreshTokenServiceImpl refreshTokenService;

    @BeforeEach
    void setUp() {
        signKey = Keys.hmacShaKeyFor(
                "test-secret-key-at-least-32-bytes-long!!".getBytes(StandardCharsets.UTF_8));
        refreshTokenService = new RefreshTokenServiceImpl(
                cacheService, jwtService, signKey, userRepository, stringRedisTemplate);
    }

    @Test
    void refresh_suspendedUser_throwsAccountSuspendedAndDeletesToken() {
        String token = "old-refresh-token";
        String email = "user@gmail.com";

        when(cacheService.get(CacheConstants.CACHE_REFRESH_TOKEN + token, String.class))
                .thenReturn(Optional.of(email));

        UserAuthProjection auth = mock(UserAuthProjection.class);
        when(auth.getStatus()).thenReturn(StatusType.SUSPENDED.name());
        when(userRepository.findAuthByEmail(email)).thenReturn(Optional.of(auth));

        @SuppressWarnings("unchecked")
        SetOperations<String, String> setOps = mock(SetOperations.class);
        when(stringRedisTemplate.opsForSet()).thenReturn(setOps);

        assertThatThrownBy(() -> refreshTokenService.refresh(token))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    org.assertj.core.api.Assertions.assertThat(appEx.getType())
                            .isEqualTo(ExceptionType.ACCOUNT_SUSPENDED);
                });

        verify(cacheService).evict(CacheConstants.CACHE_REFRESH_TOKEN + token);
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void refresh_activeUser_issuesNewTokens() {
        String token = "old-refresh-token";
        String email = "user@gmail.com";
        UUID userId = UUID.randomUUID();

        when(cacheService.get(CacheConstants.CACHE_REFRESH_TOKEN + token, String.class))
                .thenReturn(Optional.of(email));

        UserAuthProjection auth = mock(UserAuthProjection.class);
        when(auth.getId()).thenReturn(userId);
        when(auth.getEmail()).thenReturn(email);
        when(auth.getRoleName()).thenReturn("USER");
        when(auth.getStatus()).thenReturn(StatusType.ACTIVE.name());
        when(userRepository.findAuthByEmail(email)).thenReturn(Optional.of(auth));

        when(jwtService.generateToken(any())).thenReturn(
                TokenResponse.builder().token("new-access").expiresIn(new Date()).build());

        @SuppressWarnings("unchecked")
        SetOperations<String, String> setOps = mock(SetOperations.class);
        when(stringRedisTemplate.opsForSet()).thenReturn(setOps);

        refreshTokenService.refresh(token);

        verify(cacheService).evict(CacheConstants.CACHE_REFRESH_TOKEN + token);
        verify(jwtService).generateToken(any());
    }

    @Test
    void revokeAllForUser_deletesTrackedRefreshTokens() {
        String email = "user@gmail.com";
        String setKey = CacheConstants.CACHE_USER_REFRESH_TOKENS + email;

        @SuppressWarnings("unchecked")
        SetOperations<String, String> setOps = mock(SetOperations.class);
        when(stringRedisTemplate.opsForSet()).thenReturn(setOps);
        when(setOps.members(setKey)).thenReturn(Set.of("token-1", "token-2"));

        refreshTokenService.revokeAllForUser(email);

        verify(cacheService).evict(CacheConstants.CACHE_REFRESH_TOKEN + "token-1");
        verify(cacheService).evict(CacheConstants.CACHE_REFRESH_TOKEN + "token-2");
        verify(stringRedisTemplate).delete(setKey);
    }
}
