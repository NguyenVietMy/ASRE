package com.asre.asre.domain.alerts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Value object representing an event in an incident timeline.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentEvent {
    private UUID id;
    private UUID incidentId;
    private IncidentEventType eventType;
    private Map<String, Object> content; // JSON-like content
    private Instant timestamp;

    public enum IncidentEventType {
        RULE_TRIGGERED,
        METRIC_SPIKE,
        NEW_LOG_PATTERN,
        COMMENT,
        AI,
        STATUS_CHANGED
    }
}

