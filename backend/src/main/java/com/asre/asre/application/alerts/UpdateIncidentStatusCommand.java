package com.asre.asre.application.alerts;

import com.asre.asre.domain.alerts.IncidentSeverity;
import com.asre.asre.domain.alerts.IncidentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Command to update incident status and/or severity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateIncidentStatusCommand {
    private UUID incidentId;
    private UUID projectId;
    private IncidentStatus status;
    private IncidentSeverity severity;
}

