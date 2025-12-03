package com.asre.asre.domain.alerts;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for IncidentEvent.
 * Implementations in infrastructure layer.
 */
public interface IncidentEventRepository {
    /**
     * Saves an incident event.
     */
    IncidentEvent save(IncidentEvent event);

    /**
     * Finds all events for an incident, ordered by timestamp.
     */
    List<IncidentEvent> findByIncidentId(UUID incidentId);
}

