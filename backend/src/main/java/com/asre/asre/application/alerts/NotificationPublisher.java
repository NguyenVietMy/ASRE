package com.asre.asre.application.alerts;

import java.util.List;
import java.util.UUID;

/**
 * Port for publishing notification requests.
 * Implementations in infrastructure layer handle actual delivery.
 */
public interface NotificationPublisher {
    /**
     * Publishes a notification request.
     * @param projectId The project ID
     * @param ruleId The alert rule ID
     * @param serviceId The service ID
     * @param incidentId The incident ID
     * @param channels Notification channels (email addresses, webhook URLs)
     * @param eventType Event type (e.g., "INCIDENT_CREATED", "INCIDENT_RESOLVED")
     */
    void publishNotification(UUID projectId, UUID ruleId, UUID serviceId,
                            UUID incidentId, List<String> channels, String eventType);
}

