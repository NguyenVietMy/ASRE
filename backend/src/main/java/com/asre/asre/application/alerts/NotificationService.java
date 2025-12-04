package com.asre.asre.application.alerts;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Application service for publishing notification requests.
 * Delegates to NotificationPublisher port for actual delivery.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationPublisherPort notificationPublisher;

    /**
     * Requests a notification to be sent.
     */
    public void requestNotification(UUID projectId, UUID ruleId, UUID serviceId,
            UUID incidentId, List<String> channels, String eventType) {
        notificationPublisher.publishNotification(projectId, ruleId, serviceId,
                incidentId, channels, eventType);
    }
}
