package com.asre.asre.api.alerts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentEventResponse {
    private UUID id;
    private UUID incidentId;
    private String eventType;
    private Map<String, Object> content;
    private Instant timestamp;
}

