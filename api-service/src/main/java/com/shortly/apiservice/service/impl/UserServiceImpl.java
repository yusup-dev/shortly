package com.shortly.apiservice.service.impl;

import com.shortly.apiservice.dto.request.UserRegisterRequest;
import com.shortly.apiservice.dto.response.UserResponse;
import com.shortly.apiservice.entity.Role;
import com.shortly.apiservice.entity.User;
import com.shortly.apiservice.enumaration.ExceptionType;
import com.shortly.apiservice.enumaration.RoleType;
import com.shortly.apiservice.enumaration.StatusType;
import com.shortly.apiservice.exception.ApplicationException;
import com.shortly.apiservice.repository.RoleRepository;
import com.shortly.apiservice.repository.UserRepository;
import com.shortly.apiservice.service.ApiKeyService;
import com.shortly.apiservice.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final ApiKeyService apiKeyService;

    @Override @Transactional
    public UserResponse register(UserRegisterRequest userRegisterRequest) {
        if(existsByEmail(userRegisterRequest.getEmail())) {
            throw new ApplicationException(ExceptionType.EMAIL_ALREADY_EXISTS);
        }

        if(!userRegisterRequest.getPassword().equals(userRegisterRequest.getPassword())) {
            throw new ApplicationException(ExceptionType.BAD_REQUEST,
                    ExceptionType.BAD_REQUEST.getFormattedMessage("Password do not match"));
        }

        String encodedPassword = passwordEncoder.encode(userRegisterRequest.getPassword());

        Role role = roleRepository.findByName(RoleType.USER).orElseThrow(
                () -> new ApplicationException(ExceptionType.ROLE_NOT_FOUND)
        );

        User user = User.builder()
                .id(UUID.randomUUID())
                .name(userRegisterRequest.getName())
                .role(role)
                .email(userRegisterRequest.getEmail())
                .password(encodedPassword)
                .status(StatusType.ACTIVE)
                .build();

        User saved = userRepository.save(user);

        apiKeyService.createApiKey(saved.getId());

        return UserResponse.from(saved);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
