package com.asre.asre.infra.rabbitmq;

import com.asre.asre.application.alerts.NotificationPublisherPort;
import com.asre.asre.config.RabbitMQConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * RabbitMQ implementation of NotificationPublisher.
 * Publishes notification requests to RabbitMQ for async processing.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQNotificationPublisher implements NotificationPublisherPort {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void publishNotification(UUID projectId, UUID ruleId, UUID serviceId,
                                   UUID incidentId, List<String> channels, String eventType) {
        if (channels == null || channels.isEmpty()) {
            log.debug("No notification channels configured for rule {}", ruleId);
            return;
        }

        Map<String, Object> message = Map.of(
                "projectId", projectId.toString(),
                "ruleId", ruleId.toString(),
                "serviceId", serviceId.toString(),
                "incidentId", incidentId.toString(),
                "channels", channels,
                "eventType", eventType,
                "timestamp", System.currentTimeMillis()
        );

        try {
            String messageJson = objectMapper.writeValueAsString(message);
            rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATIONS_QUEUE, messageJson);
            log.debug("Published notification request for incident {}", incidentId);
        } catch (Exception e) {
            log.error("Failed to publish notification request: {}", e.getMessage(), e);
            // Don't throw - notification failure shouldn't break alert evaluation
        }
    }
}

