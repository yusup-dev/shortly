package com.shortly.apiservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.shortly.apiservice.entity.User;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    private UUID id;
    private String name;
    private String email;
    private String role;
    private String plan;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(String.valueOf(user.getRole().getName()))
                .plan(String.valueOf(user.getPlan().getName()))
                .build();
    }
}