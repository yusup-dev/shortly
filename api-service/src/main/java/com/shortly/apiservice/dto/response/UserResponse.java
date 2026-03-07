package com.shortly.apiservice.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.shortly.apiservice.entity.User;
import com.shortly.apiservice.enumaration.PlanType;
import com.shortly.apiservice.enumaration.RoleType;
import com.shortly.apiservice.enumaration.StatusType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserResponse {

    private UUID id;
    private String name;
    private String email;
    private StatusType status;
    private RoleType role;
    private PlanType plan;
    private String apiKey;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserResponse from(User user, String apiKey) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .status(user.getStatus())
                .apiKey(apiKey)
                .plan(user.getPlan().getName())
                .role(user.getRole().getName())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}