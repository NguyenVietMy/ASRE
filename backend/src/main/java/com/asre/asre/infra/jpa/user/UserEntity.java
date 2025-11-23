package com.asre.asre.infra.jpa.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity representation for User - persistence layer only.
 * No business logic, just data storage.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    private UUID id;
    private String email;
    private String passwordHash;
    private String role; // Stored as string in DB
    private Instant createdAt;
}
