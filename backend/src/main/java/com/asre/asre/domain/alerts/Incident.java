package com.asre.asre.domain.alerts;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Incident aggregate.
 * Represents an incident triggered by an alert rule.
 * Enforces business invariants through domain methods.
 */
@Getter
@Builder
public class Incident {
    private final UUID id;
    private final UUID projectId;
    private final UUID serviceId;
    private final UUID ruleId;
    private IncidentStatus status;
    private IncidentSeverity severity;
    private final Instant startedAt;
    private Instant resolvedAt;
    private final String summary;
    private String aiSummary;
    private final Instant createdAt;
    private Instant updatedAt;

    /**
     * Creates a new incident.
     */
    public static Incident create(UUID projectId, UUID serviceId, UUID ruleId,
                                 IncidentSeverity severity, String summary) {
        Instant now = Instant.now();
        return Incident.builder()
                .projectId(projectId)
                .serviceId(serviceId)
                .ruleId(ruleId)
                .status(IncidentStatus.OPEN)
                .severity(severity)
                .startedAt(now)
                .summary(summary)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * Updates status and/or severity from a command.
     */
    public void updateStatus(IncidentStatus newStatus, IncidentSeverity newSeverity) {
        if (newStatus != null) {
            transitionTo(newStatus);
        }
        if (newSeverity != null) {
            changeSeverity(newSeverity);
        }
    }

    /**
     * Changes the severity of the incident.
     */
    public void changeSeverity(IncidentSeverity newSeverity) {
        if (newSeverity == null) {
            throw new IllegalArgumentException("Severity cannot be null");
        }
        this.severity = newSeverity;
        this.updatedAt = Instant.now();
    }

    /**
     * Updates the AI summary.
     */
    public void updateAiSummary(String aiSummary) {
        this.aiSummary = aiSummary;
        this.updatedAt = Instant.now();
    }

    /**
     * Transitions the incident to a new status.
     * @throws IllegalIncidentTransitionException if the transition is not allowed
     */
    public void transitionTo(IncidentStatus newStatus) {
        if (status == null) {
            throw new IllegalIncidentTransitionException("Cannot transition from null status");
        }
        if (!status.canTransitionTo(newStatus)) {
            throw new IllegalIncidentTransitionException(
                String.format("Cannot transition incident from %s to %s", status, newStatus)
            );
        }
        this.status = newStatus;
        this.updatedAt = Instant.now();
        
        if (newStatus == IncidentStatus.RESOLVED && resolvedAt == null) {
            this.resolvedAt = Instant.now();
        }
    }

    /**
     * Ensures the incident belongs to the specified project.
     */
    public void ensureBelongsToProject(UUID projectId) {
        if (!this.projectId.equals(projectId)) {
            throw new IllegalArgumentException("Incident does not belong to project: " + projectId);
        }
    }

    /**
     * Checks if the incident is open (not resolved).
     */
    public boolean isOpen() {
        return status != IncidentStatus.RESOLVED;
    }

    /**
     * Marks the incident as resolved.
     */
    public void resolve() {
        transitionTo(IncidentStatus.RESOLVED);
    }

    /**
     * Marks the incident as investigating.
     */
    public void investigate() {
        transitionTo(IncidentStatus.INVESTIGATING);
    }

    /**
     * Updates the incident timestamp (e.g., when alert continues firing).
     */
    public void touch() {
        this.updatedAt = Instant.now();
    }
}

