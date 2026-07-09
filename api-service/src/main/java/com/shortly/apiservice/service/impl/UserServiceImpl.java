package com.shortly.apiservice.service.impl;

import com.shortly.apiservice.dto.request.UserRegisterRequest;
import com.shortly.apiservice.dto.response.UserRegisterResponse;
import com.shortly.apiservice.entity.Plan;
import com.shortly.apiservice.entity.Role;
import com.shortly.apiservice.entity.User;
import com.shortly.apiservice.enumaration.ExceptionType;
import com.shortly.apiservice.enumaration.PlanType;
import com.shortly.apiservice.enumaration.RoleType;
import com.shortly.apiservice.enumaration.StatusType;
import com.shortly.apiservice.exception.ApplicationException;
import com.shortly.apiservice.repository.PlanRepository;
import com.shortly.apiservice.repository.RoleRepository;
import com.shortly.apiservice.repository.UserRepository;
import com.shortly.apiservice.service.ApiKeyService;
import com.shortly.apiservice.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final PlanRepository planRepository;

    @Override @Transactional
    public UserRegisterResponse register(UserRegisterRequest userRegisterRequest) {
        if(existsByEmail(userRegisterRequest.getEmail())) {
            throw new ApplicationException(ExceptionType.EMAIL_ALREADY_EXISTS);
        }

        if(!userRegisterRequest.getPassword().equals(userRegisterRequest.getPasswordConfirmation())) {
            throw new ApplicationException(ExceptionType.BAD_REQUEST,
                    ExceptionType.BAD_REQUEST.getFormattedMessage("Password do not match"));
        }

        String encodedPassword = passwordEncoder.encode(userRegisterRequest.getPassword());

        Role role = roleRepository.findByName(RoleType.USER).orElseThrow(
                () -> new ApplicationException(ExceptionType.ROLE_NOT_FOUND)
        );

        Plan plan = planRepository.findByName(PlanType.FREE).orElseThrow(
                () -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND, "Plan not found")
        );

        User user = User.builder()
                .id(UUID.randomUUID())
                .name(userRegisterRequest.getName())
                .role(role)
                .plan(plan)
                .email(userRegisterRequest.getEmail())
                .password(encodedPassword)
                .status(StatusType.ACTIVE)
                .build();

        User saved = userRepository.save(user);

        String apiKey = apiKeyService.createApiKey(saved.getId());

        return UserRegisterResponse.from(saved, apiKey);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApplicationException(ExceptionType.RESOURCE_NOT_FOUND, "User not found!"));
    }
}
