package com.shortly.apiservice.dto.response;

import com.shortly.apiservice.dto.UserInfo;
import com.shortly.apiservice.enumaration.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private UUID id;
    private String name;
    private String email;
    private RoleType role;

    public static LoginResponse from(UserInfo userInfo) {
        return LoginResponse.builder()
                .id(userInfo.getUser().getId())
                .name(userInfo.getUser().getName())
                .email(userInfo.getUsername())
                .role(userInfo.getRole().getName())
                .build();
    }
}