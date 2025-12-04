package com.asre.asre.application.auth;

import com.asre.asre.domain.auth.AuthResult;
import com.asre.asre.domain.auth.LoginCommand;
import com.asre.asre.domain.auth.LogoutCommand;
import com.asre.asre.domain.auth.RefreshCommand;
import com.asre.asre.domain.auth.RefreshToken;
import com.asre.asre.domain.auth.RefreshTokenRepository;
import com.asre.asre.domain.auth.RegisterCommand;
import com.asre.asre.domain.auth.TokenServicePort;
import com.asre.asre.domain.user.User;
import com.asre.asre.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenServicePort tokenService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResult register(RegisterCommand command) {
        // Check if user already exists
        if (userRepository.findByEmail(command.getEmail()).isPresent()) {
            throw new RuntimeException("User with this email already exists");
        }

        // Create new user domain object
        User user = new User();
        user.setEmail(command.getEmail());
        user.setPasswordHash(passwordEncoder.encode(command.getPassword()));
        user.setRole(User.UserRole.MEMBER);
        user.setCreatedAt(Instant.now());
        user = userRepository.save(user);

        // Generate tokens
        String accessToken = tokenService.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = generateAndStoreRefreshTokenValue(user.getId());

        return new AuthResult(accessToken, refreshToken, user);
    }

    @Transactional
    public AuthResult login(LoginCommand command) {
        User user = userRepository.findByEmail(command.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(command.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        // Generate tokens
        String accessToken = tokenService.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = generateAndStoreRefreshTokenValue(user.getId());

        return new AuthResult(accessToken, refreshToken, user);
    }

    @Transactional
    public AuthResult refresh(RefreshCommand command) {
        if (command.getRefreshToken() == null || command.getRefreshToken().isEmpty()) {
            throw new RuntimeException("Refresh token is missing");
        }

        // Hash the token to look it up
        String tokenHash = hashToken(command.getRefreshToken());
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
        String newAccessToken = tokenService.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String newRefreshToken = generateAndStoreRefreshTokenValue(user.getId());

        return new AuthResult(newAccessToken, newRefreshToken, user);
    }

    @Transactional
    public void logout(LogoutCommand command) {
        if (command.getRefreshToken() != null && !command.getRefreshToken().isEmpty()) {
            String tokenHash = hashToken(command.getRefreshToken());
            refreshTokenRepository.deleteByTokenHash(tokenHash);
        }
    }

    private String generateAndStoreRefreshTokenValue(UUID userId) {
        // Generate random token
        String refreshToken = tokenService.generateRefreshToken();

        // Hash and store
        String tokenHash = hashToken(refreshToken);
        RefreshToken storedToken = new RefreshToken();
        storedToken.setUserId(userId);
        storedToken.setTokenHash(tokenHash);
        storedToken.setExpiresAt(Instant.now().plus(tokenService.getRefreshTokenExpirationDays(), ChronoUnit.DAYS));
        refreshTokenRepository.save(storedToken);

        return refreshToken;
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

}
