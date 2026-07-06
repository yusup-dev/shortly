package com.shortly.apiservice.controller.user;

import com.shortly.apiservice.dto.response.ApiKeyListResponse;
import com.shortly.apiservice.dto.response.ApiKeyResponse;
import com.shortly.apiservice.dto.response.ApiResponse;
import com.shortly.apiservice.entity.User;
import com.shortly.apiservice.service.ApiKeyService;
import com.shortly.apiservice.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/keys")
@RequiredArgsConstructor
@Tag(name = "User Api Key Controller")
public class UserApiKeyController {

    private final ApiKeyService apiKeyService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ApiKeyListResponse>>> list() {
        User currentUser = userService.getCurrentUser();
        List<ApiKeyListResponse> data = apiKeyService.listByUser(currentUser.getId());

        return ResponseEntity.ok(
                ApiResponse.<List<ApiKeyListResponse>>builder()
                        .success(true)
                        .message("Api keys retrieved successfully")
                        .data(data)
                        .build()
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ApiKeyResponse>> create() {
        User currentUser = userService.getCurrentUser();
        String rawKey = apiKeyService.createApiKey(currentUser.getId());

        ApiKeyResponse data = ApiKeyResponse.builder()
                .apiKey(rawKey)
                .warning("Simpan API key ini sekarang juga, kami tidak akan menampilkannya lagi!")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<ApiKeyResponse>builder()
                        .success(true)
                        .message("Api key created successfully")
                        .data(data)
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> revoke(@PathVariable UUID id) {
        User currentUser = userService.getCurrentUser();
        apiKeyService.revokeOwnApiKey(id, currentUser.getId());

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Api key revoked successfully")
                        .build()
        );
    }
}
