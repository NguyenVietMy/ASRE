package com.asre.asre.domain.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    private UUID id;
    private UUID userId;
    private String tokenHash;
    private Instant expiresAt;
    private Instant createdAt;

    // Business behavior
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(Instant.now());
    }

    public boolean isValid() {
        return !isExpired();
    }
}
