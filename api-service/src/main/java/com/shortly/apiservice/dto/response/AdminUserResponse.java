package com.shortly.apiservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.shortly.apiservice.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AdminUserResponse {
    private UUID id;
    private String name;
    private String email;
    private String role;
    private String plan;
    private String status;
    private Long totalUrls;
    private LocalDateTime createdAt;

    public static AdminUserResponse from(User user, long totalUrls) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(String.valueOf(user.getRole().getName()))
                .plan(String.valueOf(user.getPlan().getName()))
                .status(String.valueOf(user.getStatus()))
                .totalUrls(totalUrls)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
