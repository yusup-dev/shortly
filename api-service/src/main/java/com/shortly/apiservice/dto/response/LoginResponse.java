package com.shortly.apiservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.shortly.apiservice.dto.UserInfo;
import com.shortly.apiservice.entity.User;
import com.shortly.apiservice.enumaration.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {
    private UUID id;
    private String email;
    private String role;

    public static LoginResponse from(UserInfo userInfo) {
        return LoginResponse.builder()
                .id(userInfo.getId())
                .email(userInfo.getUsername())
                .role(userInfo.getRole())
                .build();
    }
}