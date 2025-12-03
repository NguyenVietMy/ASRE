package com.asre.asre.api.alerts.dto;

import com.asre.asre.domain.alerts.IncidentSeverity;
import com.asre.asre.domain.alerts.IncidentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateIncidentStatusRequest {
    private IncidentStatus status;
    private IncidentSeverity severity;
}

