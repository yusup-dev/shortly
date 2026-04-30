package com.shortly.apiservice.service.impl;

import com.shortly.apiservice.dto.UserInfo;
import com.shortly.apiservice.entity.Role;
import com.shortly.apiservice.entity.User;
import com.shortly.apiservice.enumaration.ExceptionType;
import com.shortly.apiservice.exception.ApplicationException;
import com.shortly.apiservice.repository.RoleRepository;
import com.shortly.apiservice.repository.UserRepository;
import com.shortly.apiservice.repository.projection.UserAuthProjection;
import com.shortly.apiservice.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDetailsImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final CacheService cacheService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        String cacheKey = "auth:user:" + email;

        Optional<UserInfo> cached = cacheService.get(cacheKey, UserInfo.class);
        if (cached.isPresent()) {
            return cached.get();
        }

        UserAuthProjection data = userRepository.findAuthByEmail(email).orElseThrow(
                () -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND, "User not found!")
        );

        UserInfo userInfo = UserInfo.builder()
                .id(data.getId())
                .email(data.getEmail())
                .password(data.getPassword())
                .role(data.getRoleName())
                .build();

        cacheService.put(cacheKey, userInfo, Duration.ofHours(24));

        return userInfo;
    }
}
