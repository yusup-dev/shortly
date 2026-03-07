package com.shortly.apiservice.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TokenResponse {
    private String token;
    private Date expiresIn;

    public static TokenResponse from(String token, Date expireIn) {
        return  TokenResponse.builder()
                .token(token)
                .expiresIn(expireIn)
                .build();
    }
}