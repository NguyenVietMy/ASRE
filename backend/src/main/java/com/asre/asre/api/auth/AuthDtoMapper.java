package com.asre.asre.api.auth;

import com.asre.asre.application.auth.AuthService;
import org.springframework.stereotype.Component;

@Component
public class AuthDtoMapper {

    public AuthResponse toResponse(AuthService.AuthResult result) {
        return new AuthResponse(result.getAccessToken());
    }
}

