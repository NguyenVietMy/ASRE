package com.asre.asre.domain.alerts;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Incident aggregate.
 * Implementations in infrastructure layer.
 */
public interface IncidentRepository {
    /**
     * Saves an incident.
     */
    Incident save(Incident incident);

    /**
     * Finds an incident by ID.
     */
    Optional<Incident> findById(UUID id);

    /**
     * Finds all incidents for a project.
     */
    List<Incident> findByProjectId(UUID projectId);

    /**
     * Finds all incidents for a service.
     */
    List<Incident> findByServiceId(UUID serviceId);

    /**
     * Finds all incidents for an alert rule.
     */
    List<Incident> findByRuleId(UUID ruleId);

    /**
     * Finds open incidents for a (project, rule, service) combination.
     */
    Optional<Incident> findOpenIncident(UUID projectId, UUID ruleId, UUID serviceId);

    /**
     * Finds incidents by status.
     */
    List<Incident> findByProjectIdAndStatus(UUID projectId, IncidentStatus status);
}

