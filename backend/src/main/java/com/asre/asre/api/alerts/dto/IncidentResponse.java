package com.asre.asre.api.alerts.dto;

import com.asre.asre.domain.alerts.IncidentSeverity;
import com.asre.asre.domain.alerts.IncidentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentResponse {
    private UUID id;
    private UUID projectId;
    private UUID serviceId;
    private UUID ruleId;
    private IncidentStatus status;
    private IncidentSeverity severity;
    private Instant startedAt;
    private Instant resolvedAt;
    private String summary;
    private String aiSummary;
    private Instant createdAt;
    private Instant updatedAt;
}

