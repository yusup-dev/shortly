package com.shortly.apiservice.service.impl;

import com.shortly.apiservice.dto.UserInfo;
import com.shortly.apiservice.entity.Role;
import com.shortly.apiservice.entity.User;
import com.shortly.apiservice.enumaration.ExceptionType;
import com.shortly.apiservice.exception.ApplicationException;
import com.shortly.apiservice.repository.RoleRepository;
import com.shortly.apiservice.repository.UserRepository;
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
    private final RoleRepository roleRepository;
    private final CacheService cacheService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        String USER_CACHE_KEY = "cache:user:";
        String ROLE_CACHE_KEY = "cache:role:";

        String userCacheKey = USER_CACHE_KEY + email;
        String roleCacheKey = ROLE_CACHE_KEY + email;

        Optional<User> cachedUser = cacheService.get(userCacheKey, User.class);
        Optional<Role> cachedRole = cacheService.get(roleCacheKey, Role.class);

        if (cachedUser.isPresent() && cachedRole.isPresent()) {
            User userFromDb = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ApplicationException(ExceptionType.USER_NOT_FOUND, "User not found with: " + email));

            return UserInfo.builder()
                    .user(userFromDb)
                    .role(cachedRole.get())
                    .build();
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApplicationException(ExceptionType.USER_NOT_FOUND, "User not found with: " + email));

        Role role = roleRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ApplicationException(ExceptionType.USER_NOT_FOUND, "Role not found for userId: " +  user.getId()));

        UserInfo userInfo = UserInfo.builder()
                .user(user)
                .role(role)
                .build();

        User cacheableUser = User.builder()
                .id(user.getId())
                .email(user.getEmail())
                .build();

        cacheService.put(userCacheKey, cacheableUser, Duration.ofMinutes(10));
        cacheService.put(roleCacheKey, role, Duration.ofMinutes(10));

        return userInfo;
    }
}
