package com.asre.asre.domain.ingestion;

/**
 * Port for publishing messages to async processing queue.
 * Implementation will be in infrastructure layer.
 */
public interface MessagePublisherPort {
    void publishMetricsMessage(String queueName, String message);
    void publishLogsMessage(String queueName, String message);
}
