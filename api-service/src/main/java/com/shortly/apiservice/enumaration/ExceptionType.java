package com.shortly.apiservice.enumaration;

import lombok.Getter;

@Getter
public enum ExceptionType {

    RESOURCE_NOT_FOUND("Resource not found", 404),
    USER_NOT_FOUND("User not found", 404),
    ROLE_NOT_FOUND("Default role not found", 404),
    BAD_REQUEST("Bad request", 400),
    EMAIL_ALREADY_EXISTS("Email is already taken", 409),
    INVALID_PASSWORD("Current password is incorrect", 401),
    FORBIDDEN("Access denied", 403),
    INTERNAL_SERVER_ERROR("Internal server error", 500),
    RATE_LIMIT_EXCEEDED("Limit harian tercapai", 429),
    UNAUTHORIZED("Unauthorized access", 401),

    // Auth
    INVALID_CREDENTIALS("Email atau password salah", 401),
    ACCOUNT_SUSPENDED("Akun kamu di-suspend. Hubungi admin.", 403),
    VALIDATION_ERROR("Validasi gagal", 400),

    // URL
    INVALID_URL("URL tidak valid", 400),
    ALIAS_ALREADY_TAKEN("Alias sudah digunakan", 409),
    SHORT_URL_NOT_FOUND("URL tidak ditemukan", 404),
    URL_EXPIRED("URL ini sudah kedaluwarsa", 410),
    URL_SUSPENDED("URL ini tidak tersedia", 403),
    QUOTA_EXCEEDED("Kuota URL tercapai untuk plan kamu", 429),
    BULK_LIMIT_EXCEEDED("Maksimal URL per request melebihi batas plan kamu", 429),
    NOT_PRO_PLAN("Fitur ini hanya untuk plan Pro", 403),

    // API Key
    MISSING_API_KEY("Missing API KEY", 400),
    INVALID_API_KEY("Invalid or expired API key", 400);

    private final String message;
    private final int httpCode;

    ExceptionType(String message, int httpCode) {
        this.message = message;
        this.httpCode = httpCode;
    }

    public String getFormattedMessage(String context) {
        return String.format("%s: %s", message, context);
    }
}
