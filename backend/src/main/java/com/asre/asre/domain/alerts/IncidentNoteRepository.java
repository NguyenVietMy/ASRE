package com.asre.asre.domain.alerts;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for IncidentNote.
 * Implementations in infrastructure layer.
 */
public interface IncidentNoteRepository {
    /**
     * Saves an incident note.
     */
    IncidentNote save(IncidentNote note);

    /**
     * Finds all notes for an incident, ordered by creation time.
     */
    List<IncidentNote> findByIncidentId(UUID incidentId);
}

