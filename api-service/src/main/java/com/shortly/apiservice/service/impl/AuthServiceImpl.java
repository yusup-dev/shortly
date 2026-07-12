package com.shortly.apiservice.service.impl;

import com.shortly.apiservice.dto.UserInfo;
import com.shortly.apiservice.dto.request.AuthRequest;
import com.shortly.apiservice.enumaration.ExceptionType;
import com.shortly.apiservice.exception.ApplicationException;
import com.shortly.apiservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;

    @Override
    public UserInfo authenticate(AuthRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail(),
                            authRequest.getPassword())
            );

            return (UserInfo) authentication.getPrincipal();
        } catch (DisabledException e) {
            throw new ApplicationException(ExceptionType.ACCOUNT_SUSPENDED);
        } catch (BadCredentialsException e) {
            throw new ApplicationException(ExceptionType.INVALID_CREDENTIALS);
        } catch (ApplicationException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Authentication failed for email {}: {}", authRequest.getEmail(), e.getMessage());
            throw new ApplicationException(ExceptionType.INVALID_CREDENTIALS);
        }
    }
}
