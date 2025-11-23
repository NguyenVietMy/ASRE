package com.asre.asre.application.auth;

import com.asre.asre.domain.auth.RefreshToken;
import com.asre.asre.domain.auth.RefreshTokenRepository;
import com.asre.asre.domain.user.User;
import com.asre.asre.domain.user.UserRepository;
import com.asre.asre.infra.security.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResult register(String email, String password, HttpServletResponse response) {
        // Check if user already exists
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("User with this email already exists");
        }

        // Create new user domain object
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(User.UserRole.MEMBER);
        user.setCreatedAt(Instant.now());
        user = userRepository.save(user);

        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        generateAndStoreRefreshToken(user.getId(), response);

        return new AuthResult(accessToken, user);
    }

    @Transactional
    public AuthResult login(String email, String password, HttpServletResponse response) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        generateAndStoreRefreshToken(user.getId(), response);

        return new AuthResult(accessToken, user);
    }

    @Transactional
    public AuthResult refresh(String refreshTokenCookie, HttpServletResponse response) {
        if (refreshTokenCookie == null || refreshTokenCookie.isEmpty()) {
            throw new RuntimeException("Refresh token is missing");
        }

        // Hash the token to look it up
        String tokenHash = hashToken(refreshTokenCookie);
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        // Check if token is expired using domain behavior
        if (storedToken.isExpired()) {
            refreshTokenRepository.deleteByTokenHash(tokenHash);
            throw new RuntimeException("Refresh token has expired");
        }

        // Get user
        User user = userRepository.findById(storedToken.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Delete old refresh token (rotation)
        refreshTokenRepository.deleteByTokenHash(tokenHash);

        // Generate new tokens
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        generateAndStoreRefreshToken(user.getId(), response);

        return new AuthResult(newAccessToken, user);
    }

    @Transactional
    public void logout(String refreshTokenCookie, HttpServletResponse response) {
        if (refreshTokenCookie != null && !refreshTokenCookie.isEmpty()) {
            String tokenHash = hashToken(refreshTokenCookie);
            refreshTokenRepository.deleteByTokenHash(tokenHash);
        }

        // Clear refresh token cookie
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    private void generateAndStoreRefreshToken(UUID userId, HttpServletResponse response) {
        // Generate random token
        String refreshToken = jwtUtil.generateRefreshToken();

        // Hash and store
        String tokenHash = hashToken(refreshToken);
        RefreshToken storedToken = new RefreshToken();
        storedToken.setUserId(userId);
        storedToken.setTokenHash(tokenHash);
        storedToken.setExpiresAt(Instant.now().plus(jwtUtil.getRefreshTokenExpirationDays(), ChronoUnit.DAYS));
        refreshTokenRepository.save(storedToken);

        // Set HttpOnly cookie
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true) // Set to true in production with HTTPS
                .path("/")
                .maxAge(Duration.ofDays(jwtUtil.getRefreshTokenExpirationDays()))
                .sameSite("Strict")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error hashing token", e);
        }
    }

    // Result class for auth operations
    @lombok.Value
    public static class AuthResult {
        String accessToken;
        User user;
    }
}
