package com.asre.asre.domain.alerts;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for AlertRule aggregate.
 * Implementations in infrastructure layer.
 */
public interface AlertRuleRepository {
    /**
     * Saves an alert rule.
     */
    AlertRule save(AlertRule alertRule);

    /**
     * Finds an alert rule by ID.
     */
    Optional<AlertRule> findById(UUID id);

    /**
     * Finds all alert rules for a project.
     */
    List<AlertRule> findByProjectId(UUID projectId);

    /**
     * Finds all alert rules for a service.
     */
    List<AlertRule> findByServiceId(UUID serviceId);

    /**
     * Finds all active (enabled) alert rules.
     */
    List<AlertRule> findActiveRules();

    /**
     * Deletes an alert rule.
     */
    void delete(UUID id);
}

