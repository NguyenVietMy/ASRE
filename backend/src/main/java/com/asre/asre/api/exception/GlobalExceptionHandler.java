package com.asre.asre.api.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        // Log only the message, not the full stack trace
        String message = e.getMessage();

        // Determine HTTP status based on error message
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        if (message != null) {
            if (message.contains("Invalid email or password") ||
                    message.contains("Invalid refresh token") ||
                    message.contains("Refresh token has expired") ||
                    message.contains("Refresh token is missing")) {
                status = HttpStatus.UNAUTHORIZED;
            } else if (message.contains("User with this email already exists")) {
                status = HttpStatus.CONFLICT;
            }
        }

        // Log only one line - just the message
        log.error("Error: {}", message);

        return ResponseEntity.status(status)
                .body(Map.of("error", message != null ? message : "An error occurred"));
    }
}
