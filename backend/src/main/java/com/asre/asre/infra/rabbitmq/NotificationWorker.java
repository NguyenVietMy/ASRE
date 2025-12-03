package com.asre.asre.infra.rabbitmq;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Worker that processes notification requests.
 * For MVP, logs notifications. Email/webhook implementation can be added later.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationWorker {

    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "notifications.send")
    public void handleNotification(String message) {
        try {
            Map<String, Object> payload = objectMapper.readValue(message, new TypeReference<Map<String, Object>>() {});
            
            String eventType = (String) payload.get("eventType");
            String incidentId = (String) payload.get("incidentId");
            @SuppressWarnings("unchecked")
            List<String> channels = (List<String>) payload.get("channels");

            log.info("Notification requested: eventType={}, incidentId={}, channels={}", 
                    eventType, incidentId, channels);

            // TODO: Implement email sending
            // TODO: Implement webhook calls
            
            // For MVP, just log
            for (String channel : channels) {
                if (channel.startsWith("http://") || channel.startsWith("https://")) {
                    log.info("Would send webhook to: {}", channel);
                } else if (channel.contains("@")) {
                    log.info("Would send email to: {}", channel);
                }
            }
            
        } catch (Exception e) {
            log.error("Failed to process notification: {}", e.getMessage(), e);
        }
    }
}

