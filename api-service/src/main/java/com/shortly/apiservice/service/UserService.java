package com.shortly.apiservice.service;

import com.shortly.apiservice.dto.request.UserRegisterRequest;
import com.shortly.apiservice.dto.response.UserRegisterResponse;
import com.shortly.apiservice.dto.response.UserResponse;
import com.shortly.apiservice.entity.User;

public interface UserService {
    UserRegisterResponse register(UserRegisterRequest userRegisterRequest);
    boolean existsByEmail(String email);
    User getCurrentUser();
}
