package com.shortly.apiservice.service;

import com.shortly.apiservice.dto.UserInfo;
import com.shortly.apiservice.dto.request.AuthRequest;
public interface AuthService {
    UserInfo authenticate(AuthRequest authRequest);
}
