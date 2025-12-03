package com.asre.asre.application.alerts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Command to add a note to an incident.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddIncidentNoteCommand {
    private UUID incidentId;
    private UUID projectId;
    private UUID authorUserId;
    private String content;
}

