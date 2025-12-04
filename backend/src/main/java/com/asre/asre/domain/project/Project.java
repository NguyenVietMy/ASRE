package com.asre.asre.domain.project;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    private UUID id;
    private String name;
    private String description;
    private String apiKey;
    private UUID ownerUserId;
    private Integer rateLimitPerMinute;
    private Instant createdAt;
    private Instant deletedAt;

    public boolean isValidApiKey(String apiKey) {
        return this.apiKey != null && this.apiKey.equals(apiKey);
    }

    /**
     * Checks if the project is deleted (soft delete).
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Soft deletes the project.
     */
    public void softDelete() {
        this.deletedAt = Instant.now();
    }

    /**
     * Ensures the project belongs to the specified user.
     */
    public void ensureOwnedBy(UUID userId) {
        if (!this.ownerUserId.equals(userId)) {
            throw new IllegalArgumentException("Project does not belong to user: " + userId);
        }
    }
}
