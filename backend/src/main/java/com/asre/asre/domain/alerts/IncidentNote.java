package com.asre.asre.domain.alerts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Value object representing a note/comment on an incident.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentNote {
    private UUID id;
    private UUID incidentId;
    private UUID authorUserId;
    private String content;
    private Instant createdAt;
}

