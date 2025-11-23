package com.asre.asre.domain.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private UUID id;
    private String email;
    private String passwordHash;
    private UserRole role;
    private Instant createdAt;

    public enum UserRole {
        ADMIN, MEMBER
    }

    // Business behavior
    public void changePassword(String newPasswordHash) {
        if (newPasswordHash == null || newPasswordHash.isBlank()) {
            throw new IllegalArgumentException("Password hash cannot be null or empty");
        }
        this.passwordHash = newPasswordHash;
    }

    public void promoteToAdmin() {
        this.role = UserRole.ADMIN;
    }

    public void demoteToMember() {
        this.role = UserRole.MEMBER;
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }
}
