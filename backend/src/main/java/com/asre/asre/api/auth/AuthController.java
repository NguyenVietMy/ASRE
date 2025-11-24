package com.asre.asre.api.auth;

import com.asre.asre.application.auth.AuthService;
import com.asre.asre.domain.auth.AuthResult;
import com.asre.asre.domain.auth.LoginCommand;
import com.asre.asre.domain.auth.LogoutCommand;
import com.asre.asre.domain.auth.RefreshCommand;
import com.asre.asre.domain.auth.RegisterCommand;
import com.asre.asre.infra.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthDtoMapper mapper;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request,
            HttpServletResponse response) {
        RegisterCommand command = mapper.toRegisterCommand(request);
        AuthResult result = authService.register(command);
        setRefreshTokenCookie(result.getRefreshToken(), response);
        return ResponseEntity.ok(mapper.toResponse(result));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request,
            HttpServletResponse response) {
        LoginCommand command = mapper.toLoginCommand(request);
        AuthResult result = authService.login(command);
        setRefreshTokenCookie(result.getRefreshToken(), response);
        return ResponseEntity.ok(mapper.toResponse(result));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request,
            HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookie(request);
        RefreshCommand command = mapper.toRefreshCommand(refreshToken);
        AuthResult result = authService.refresh(command);
        setRefreshTokenCookie(result.getRefreshToken(), response);
        return ResponseEntity.ok(mapper.toResponse(result));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request,
            HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookie(request);
        LogoutCommand command = mapper.toLogoutCommand(refreshToken);
        authService.logout(command);
        clearRefreshTokenCookie(response);
        return ResponseEntity.ok().build();
    }

    private void setRefreshTokenCookie(String refreshToken, HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true) // Set to true in production with HTTPS
                .path("/")
                .maxAge(Duration.ofDays(jwtUtil.getRefreshTokenExpirationDays()))
                .sameSite("Strict")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
