package com.asre.asre.infra.jpa.alerts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity representation for Incident - persistence layer only.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentEntity {
    private UUID id;
    private UUID projectId;
    private UUID serviceId;
    private UUID ruleId;
    private String status; // Stored as string (incident_status enum)
    private String severity; // Stored as string (incident_severity enum)
    private Instant startedAt;
    private Instant resolvedAt;
    private String summary;
    private String aiSummary;
    private Instant createdAt;
    private Instant updatedAt;
}

