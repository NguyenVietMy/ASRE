package com.asre.asre.domain.auth;

import java.util.UUID;

/**
 * Domain port for token generation and validation.
 * Abstracts JWT implementation details from domain/application layers.
 */
public interface TokenServicePort {
    /**
     * Generate an access token for a user.
     */
    String generateAccessToken(UUID userId, String email, String role);

    /**
     * Generate a refresh token value (random string).
     */
    String generateRefreshToken();

    /**
     * Extract user ID from an access token.
     */
    UUID extractUserId(String token);

    /**
     * Extract email from an access token.
     */
    String extractEmail(String token);

    /**
     * Extract role from an access token.
     */
    String extractRole(String token);

    /**
     * Validate an access token.
     */
    boolean validateToken(String token);

    /**
     * Get refresh token expiration in days (for cookie TTL).
     */
    int getRefreshTokenExpirationDays();
}

