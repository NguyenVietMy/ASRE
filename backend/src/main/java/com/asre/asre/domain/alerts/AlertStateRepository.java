package com.asre.asre.domain.alerts;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for AlertState (Redis-backed).
 * Used to track alert firing state and duration for "for duration" logic.
 */
public interface AlertStateRepository {
    /**
     * Saves alert state.
     */
    void save(AlertState state, int ttlSeconds);

    /**
     * Finds alert state by key components.
     */
    Optional<AlertState> findByKey(UUID projectId, UUID ruleId, UUID serviceId);

    /**
     * Deletes alert state.
     */
    void delete(UUID projectId, UUID ruleId, UUID serviceId);
}

