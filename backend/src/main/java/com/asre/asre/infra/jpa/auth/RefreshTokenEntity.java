package com.asre.asre.infra.jpa.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity representation for RefreshToken - persistence layer only.
 * No business logic, just data storage.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenEntity {
    private UUID id;
    private UUID userId;
    private String tokenHash;
    private Instant expiresAt;
    private Instant createdAt;
}
