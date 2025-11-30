package com.asre.asre.domain.service;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Service aggregate.
 * Implementations in infrastructure layer.
 */
public interface ServiceRepository {
    /**
     * Find a service by project and name.
     */
    Optional<Service> findByProjectIdAndName(UUID projectId, String name);

    /**
     * Find a service by ID.
     */
    Optional<Service> findById(UUID id);

    /**
     * Save a service (create or update).
     */
    Service save(Service service);

    /**
     * Check if a service exists for the given project and name.
     */
    boolean existsByProjectIdAndName(UUID projectId, String name);
}

