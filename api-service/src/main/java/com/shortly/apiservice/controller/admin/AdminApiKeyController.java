package com.shortly.apiservice.controller.admin;

import com.shortly.apiservice.dto.response.ApiKeyResponse;
import com.shortly.apiservice.dto.response.ApiResponse;
import com.shortly.apiservice.service.ApiKeyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/api-keys")
@RequiredArgsConstructor
@Tag(name = "Admin Api Key Controller")
@PreAuthorize("hasRole('ADMIN')")
public class AdminApiKeyController {

    private final ApiKeyService apiKeyService;

    @PostMapping("/{id}/rotate")
    public ResponseEntity<ApiResponse<ApiKeyResponse>> rotate(
            @PathVariable UUID id
    ) {
        ApiKeyResponse data = apiKeyService.updateApiKey(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<ApiKeyResponse>builder()
                        .success(true)
                        .message("Rotate api key successfully!")
                        .data(data)
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> revoke(
            @PathVariable UUID id
    ) {
        apiKeyService.revokeApiKey(id);
        return ResponseEntity.ok(
                ApiResponse.<Object>builder()
                        .success(true)
                        .message("Revoke api key successfully!")
                        .build()
        );
    }
}
