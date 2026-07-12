package com.shortly.apiservice.service.impl;

import com.shortly.apiservice.constant.CacheConstants;
import com.shortly.apiservice.dto.request.UpdateQuotaRequest;
import com.shortly.apiservice.entity.ApiKey;
import com.shortly.apiservice.entity.Plan;
import com.shortly.apiservice.entity.Quota;
import com.shortly.apiservice.entity.Role;
import com.shortly.apiservice.entity.User;
import com.shortly.apiservice.enumaration.ActionType;
import com.shortly.apiservice.enumaration.ExceptionType;
import com.shortly.apiservice.enumaration.KeyStatusType;
import com.shortly.apiservice.enumaration.PlanType;
import com.shortly.apiservice.enumaration.RoleType;
import com.shortly.apiservice.enumaration.StatusType;
import com.shortly.apiservice.enumaration.TargetType;
import com.shortly.apiservice.exception.ApplicationException;
import com.shortly.apiservice.repository.ApiKeyRepository;
import com.shortly.apiservice.repository.QuotaRepository;
import com.shortly.apiservice.repository.UrlClickRepository;
import com.shortly.apiservice.repository.UrlRepository;
import com.shortly.apiservice.repository.UserRepository;
import com.shortly.apiservice.service.AuditLogService;
import com.shortly.apiservice.service.CacheService;
import com.shortly.apiservice.service.RefreshTokenService;
import com.shortly.apiservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private UrlRepository urlRepository;
    @Mock private UrlClickRepository urlClickRepository;
    @Mock private ApiKeyRepository apiKeyRepository;
    @Mock private QuotaRepository quotaRepository;
    @Mock private AuditLogService auditLogService;
    @Mock private CacheService cacheService;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private UserService userService;
    @Mock private HttpServletRequest httpServletRequest;

    @InjectMocks
    private AdminServiceImpl adminService;

    @Test
    void updateUserStatus_suspendUser_evictsCacheAndRevokesRefreshTokens() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId, "user@gmail.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(urlRepository.countByUserId(userId)).thenReturn(3L);
        when(userService.getCurrentUser()).thenReturn(activeUser(UUID.randomUUID(), "admin@gmail.com"));

        adminService.updateUserStatus(userId, "SUSPENDED", httpServletRequest);

        assertThat(user.getStatus()).isEqualTo(StatusType.SUSPENDED);
        verify(cacheService).evict(CacheConstants.CACHE_AUTH + "user@gmail.com");
        verify(refreshTokenService).revokeAllForUser("user@gmail.com");
        verify(auditLogService).saveAuditLog(
                httpServletRequest, ActionType.SUSPEND_USER, TargetType.USER, userId);
    }

    @Test
    void updateUserStatus_activateUser_doesNotRevokeRefreshTokens() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId, "user@gmail.com");
        user.setStatus(StatusType.SUSPENDED);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(urlRepository.countByUserId(userId)).thenReturn(0L);

        adminService.updateUserStatus(userId, "ACTIVE", httpServletRequest);

        assertThat(user.getStatus()).isEqualTo(StatusType.ACTIVE);
        verify(refreshTokenService, never()).revokeAllForUser(any());
        verify(auditLogService).saveAuditLog(
                httpServletRequest, ActionType.ACTIVATE_USER, TargetType.USER, userId);
    }

    @Test
    void updateUserStatus_selfSuspend_isRejected() {
        UUID adminId = UUID.randomUUID();
        User admin = activeUser(adminId, "admin@gmail.com");

        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(userService.getCurrentUser()).thenReturn(admin);

        assertThatThrownBy(() -> adminService.updateUserStatus(adminId, "SUSPENDED", httpServletRequest))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getType())
                        .isEqualTo(ExceptionType.BAD_REQUEST));

        verify(userRepository, never()).save(any());
        verify(refreshTokenService, never()).revokeAllForUser(any());
    }

    @Test
    void updateUserStatus_invalidStatus_isRejected() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId, "user@gmail.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> adminService.updateUserStatus(userId, "DELETED", httpServletRequest))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getType())
                        .isEqualTo(ExceptionType.VALIDATION_ERROR));
    }

    @Test
    void updateUserStatus_deletedUser_isNotFound() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId, "user@gmail.com");
        user.setDeletedAt(LocalDateTime.now());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> adminService.updateUserStatus(userId, "SUSPENDED", httpServletRequest))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getType())
                        .isEqualTo(ExceptionType.USER_NOT_FOUND));
    }

    @Test
    void updateUserQuota_updatesOnlyActiveApiKeysAndEvictsPlanCache() {
        UUID userId = UUID.randomUUID();
        User user = activeUser(userId, "user@gmail.com");

        ApiKey activeKey = apiKey(userId, KeyStatusType.ACTIVE, "hash-active");
        ApiKey revokedKey = apiKey(userId, KeyStatusType.REVOKED, "hash-revoked");
        Quota quota = Quota.builder()
                .id(UUID.randomUUID())
                .apiKey(activeKey)
                .maxRequestsPerDay(100)
                .maxUrlsPerKey(10)
                .maxBulk(0)
                .build();

        UpdateQuotaRequest request = new UpdateQuotaRequest();
        request.setMaxRequestsPerDay(5000);
        request.setMaxUrlsPerKey(500);
        request.setMaxBulk(50);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(apiKeyRepository.findByUserId(userId)).thenReturn(List.of(activeKey, revokedKey));
        when(quotaRepository.findByApiKeyId(activeKey.getId())).thenReturn(Optional.of(quota));
        when(urlRepository.countByUserId(userId)).thenReturn(1L);

        adminService.updateUserQuota(userId, request, httpServletRequest);

        assertThat(quota.getMaxRequestsPerDay()).isEqualTo(5000);
        assertThat(quota.getMaxUrlsPerKey()).isEqualTo(500);
        assertThat(quota.getMaxBulk()).isEqualTo(50);
        verify(quotaRepository, never()).findByApiKeyId(revokedKey.getId());
        verify(cacheService).evict(CacheConstants.CACHE_PLAN + "hash-active");
        verify(cacheService, never()).evict(CacheConstants.CACHE_PLAN + "hash-revoked");
        verify(auditLogService).saveAuditLog(
                httpServletRequest, ActionType.UPDATE_QUOTA, TargetType.USER, userId);
    }

    @Test
    void getMetrics_includesInactiveCounts() {
        when(userRepository.countByDeletedAtIsNull()).thenReturn(10L);
        when(userRepository.countByStatusAndDeletedAtIsNull(StatusType.ACTIVE)).thenReturn(7L);
        when(userRepository.countByStatusAndDeletedAtIsNull(StatusType.SUSPENDED)).thenReturn(2L);
        when(userRepository.countByStatusAndDeletedAtIsNull(StatusType.INACTIVE)).thenReturn(1L);
        when(urlRepository.countAll()).thenReturn(20L);
        when(urlRepository.countActive(any())).thenReturn(15L);
        when(urlRepository.countExpired(any())).thenReturn(3L);
        when(urlRepository.countSuspended()).thenReturn(2L);
        when(urlClickRepository.countAllTime()).thenReturn(100L);
        when(urlClickRepository.countToday()).thenReturn(5L);
        when(apiKeyRepository.countAll()).thenReturn(12L);
        when(apiKeyRepository.countActive()).thenReturn(8L);

        var metrics = adminService.getMetrics();

        assertThat(metrics.getUsers().getTotal()).isEqualTo(10);
        assertThat(metrics.getUsers().getInactive()).isEqualTo(1);
        assertThat(metrics.getApiKeys().getInactive()).isEqualTo(4);
    }

    private User activeUser(UUID id, String email) {
        return User.builder()
                .id(id)
                .name("Test User")
                .email(email)
                .password("encoded")
                .status(StatusType.ACTIVE)
                .role(Role.builder().name(RoleType.USER).description("User").build())
                .plan(Plan.builder().name(PlanType.FREE).maxRequestsPerDay(100).maxUrlsPerKey(10).maxBulk(0).build())
                .build();
    }

    private ApiKey apiKey(UUID userId, KeyStatusType status, String hash) {
        User user = User.builder().id(userId).build();
        return ApiKey.builder()
                .id(UUID.randomUUID())
                .user(user)
                .keyHash(hash)
                .status(status)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
    }
}
