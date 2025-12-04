package com.asre.asre.infra.security;

import com.asre.asre.domain.auth.TokenServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Infrastructure implementation of TokenService using JWT.
 */
@Component
@RequiredArgsConstructor
public class JwtTokenService implements TokenServicePort {

    private final JwtUtil jwtUtil;

    @Override
    public String generateAccessToken(UUID userId, String email, String role) {
        return jwtUtil.generateAccessToken(userId, email, role);
    }

    @Override
    public String generateRefreshToken() {
        return jwtUtil.generateRefreshToken();
    }

    @Override
    public UUID extractUserId(String token) {
        return jwtUtil.extractUserId(token);
    }

    @Override
    public String extractEmail(String token) {
        return jwtUtil.extractEmail(token);
    }

    @Override
    public String extractRole(String token) {
        return jwtUtil.extractRole(token);
    }

    @Override
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    @Override
    public int getRefreshTokenExpirationDays() {
        return jwtUtil.getRefreshTokenExpirationDays();
    }
}

