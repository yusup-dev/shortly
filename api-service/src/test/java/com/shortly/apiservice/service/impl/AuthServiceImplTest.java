package com.shortly.apiservice.service.impl;

import com.shortly.apiservice.dto.UserInfo;
import com.shortly.apiservice.dto.request.AuthRequest;
import com.shortly.apiservice.enumaration.ExceptionType;
import com.shortly.apiservice.enumaration.StatusType;
import com.shortly.apiservice.exception.ApplicationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void authenticate_disabledUser_returnsAccountSuspended() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new DisabledException("User disabled"));

        AuthRequest request = new AuthRequest();
        request.setEmail("user@gmail.com");
        request.setPassword("password");

        assertThatThrownBy(() -> authService.authenticate(request))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getType())
                        .isEqualTo(ExceptionType.ACCOUNT_SUSPENDED));
    }

    @Test
    void authenticate_badCredentials_returnsInvalidCredentials() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        AuthRequest request = new AuthRequest();
        request.setEmail("user@gmail.com");
        request.setPassword("wrong");

        assertThatThrownBy(() -> authService.authenticate(request))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getType())
                        .isEqualTo(ExceptionType.INVALID_CREDENTIALS));
    }

    @Test
    void authenticate_activeUser_returnsUserInfo() {
        UserInfo userInfo = UserInfo.builder()
                .id(UUID.randomUUID())
                .email("user@gmail.com")
                .role("USER")
                .status(StatusType.ACTIVE.name())
                .build();

        Authentication authentication = new UsernamePasswordAuthenticationToken(userInfo, null);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        AuthRequest request = new AuthRequest();
        request.setEmail("user@gmail.com");
        request.setPassword("password");

        UserInfo result = authService.authenticate(request);

        assertThat(result.getEmail()).isEqualTo("user@gmail.com");
        assertThat(result.getStatus()).isEqualTo(StatusType.ACTIVE.name());
    }
}
