package com.shortly.apiservice.exception;

import com.shortly.apiservice.dto.response.ErrorResponse;
import com.shortly.apiservice.enumaration.ExceptionType;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.nio.file.AccessDeniedException;
import java.security.SignatureException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GenericExceptionHandler {
    @ExceptionHandler(ApplicationException.class)
    public @ResponseBody ErrorResponse handleApplicationException(HttpServletRequest req,
                                                                  HttpServletResponse resp,
                                                                  ApplicationException exception) {
        ExceptionType type = exception.getType();
        HttpStatus status = HttpStatus.resolve(type.getHttpCode());

        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR; // Default to 500 if no valid status is found
        }

        log.error("ApplicationException handled: {} - {}", status.value(), exception.getMessage());

        resp.setStatus(status.value());

        return ErrorResponse.builder()
                .code(status.value())
                .message(exception.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler({
            AccessDeniedException.class,
            BadCredentialsException.class,
            SignatureException.class,
            ExpiredJwtException.class,
            AuthenticationException.class,
            InsufficientAuthenticationException.class
    })
    public @ResponseBody ErrorResponse handleAuthenticationExceptions(HttpServletRequest req,
                                                                      HttpServletResponse resp,
                                                                      Exception exception) {
        log.error("Authentication exception: {} - {}", HttpStatus.FORBIDDEN.value(), exception.getMessage());

        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);

        return ErrorResponse.builder()
                .code(HttpStatus.FORBIDDEN.value())
                .message(exception.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(objectError -> {
            String fieldName = ((FieldError) objectError).getField();
            String errorMessage = objectError.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.error("Validation error: {}", errors);

        return ErrorResponse.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed")
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(Exception.class)
    public @ResponseBody ErrorResponse handleGenericException(HttpServletRequest req,
                                                              HttpServletResponse resp,
                                                              Exception exception) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        if (exception instanceof AccessDeniedException || exception instanceof SignatureException || exception instanceof ExpiredJwtException || exception instanceof AuthenticationException) {
            status = HttpStatus.FORBIDDEN;
        }

        log.error("Generic exception: {} - {}", status.value(), exception.getMessage());

        resp.setStatus(status.value());

        return ErrorResponse.builder()
                .code(status.value())
                .message(exception.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
