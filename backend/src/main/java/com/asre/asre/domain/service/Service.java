package com.asre.asre.domain.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Service aggregate - represents a service within a project.
 * Services are auto-discovered from ingested metrics and logs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Service {
    private UUID id;
    private UUID projectId;
    private String name;
    private Instant createdAt;
    private Instant lastSeenAt;

    /**
     * Creates a new service for auto-discovery.
     * @param projectId The project this service belongs to
     * @param name The service name (from metrics/logs)
     * @return A new Service instance
     */
    public static Service create(UUID projectId, String name) {
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Service name cannot be null or blank");
        }
        
        Service service = new Service();
        service.setProjectId(projectId);
        service.setName(name.trim());
        service.setCreatedAt(Instant.now());
        service.setLastSeenAt(Instant.now());
        return service;
    }

    /**
     * Updates the last seen timestamp when service is detected in new telemetry.
     */
    public void updateLastSeen() {
        this.lastSeenAt = Instant.now();
    }

    /**
     * Validates that this service belongs to the given project.
     * Used for multi-tenant isolation at domain level.
     */
    public void ensureBelongsToProject(UUID projectId) {
        if (!this.projectId.equals(projectId)) {
            throw new IllegalArgumentException("Service does not belong to the specified project");
        }
    }
}

