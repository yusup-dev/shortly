package com.shortly.apiservice.controller;

import com.shortly.apiservice.dto.UserInfo;
import com.shortly.apiservice.dto.request.AuthRequest;
import com.shortly.apiservice.dto.response.ApiKeyResponse;
import com.shortly.apiservice.dto.response.ApiResponse;
import com.shortly.apiservice.dto.response.AuthResponse;
import com.shortly.apiservice.dto.response.TokenResponse;
import com.shortly.apiservice.enumaration.ExceptionType;
import com.shortly.apiservice.exception.ApplicationException;
import com.shortly.apiservice.service.ApiKeyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api-key")
@RequiredArgsConstructor
@Tag(name = "Api Key Controller")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @PostMapping("/{id}/rotate")
    public ResponseEntity<ApiResponse<ApiKeyResponse>> rotate(
            @PathVariable UUID id
    ) {
        try {
            ApiKeyResponse data = apiKeyService.updateApiKey(id);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.<ApiKeyResponse>builder()
                            .success(true)
                            .message("Rotate api key successfully!")
                            .data(data)
                            .build()
            );
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ApplicationException(ExceptionType.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> revoke(
            @PathVariable UUID id
    ) {
        try {
            apiKeyService.revokeApiKey(id);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.<Object>builder()
                            .success(true)
                            .message("Revoke api key successfully!")
                            .build()
            );
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new ApplicationException(ExceptionType.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

}
