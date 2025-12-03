package com.asre.asre.api.alerts.dto;

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
public class IncidentNoteResponse {
    private UUID id;
    private UUID incidentId;
    private UUID authorUserId;
    private String content;
    private Instant createdAt;
}

