package com.asre.asre.api.auth;

import com.asre.asre.domain.auth.AuthResult;
import com.asre.asre.application.auth.LoginCommand;
import com.asre.asre.application.auth.LogoutCommand;
import com.asre.asre.application.auth.RefreshCommand;
import com.asre.asre.application.auth.RegisterCommand;
import org.springframework.stereotype.Component;

@Component
public class AuthDtoMapper {

    public AuthResponse toResponse(AuthResult result) {
        return new AuthResponse(result.getAccessToken());
    }

    public LoginCommand toLoginCommand(AuthRequest request) {
        return new LoginCommand(request.getEmail(), request.getPassword());
    }

    public RegisterCommand toRegisterCommand(RegisterRequest request) {
        return new RegisterCommand(request.getEmail(), request.getPassword());
    }

    public RefreshCommand toRefreshCommand(String refreshToken) {
        return new RefreshCommand(refreshToken);
    }

    public LogoutCommand toLogoutCommand(String refreshToken) {
        return new LogoutCommand(refreshToken);
    }
}
