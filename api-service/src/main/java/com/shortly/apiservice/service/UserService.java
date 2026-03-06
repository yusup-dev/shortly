package com.shortly.apiservice.service;

import com.shortly.apiservice.dto.request.UserRegisterRequest;
import com.shortly.apiservice.dto.response.UserResponse;

public interface UserService {
    UserResponse register(UserRegisterRequest userRegisterRequest);
}
