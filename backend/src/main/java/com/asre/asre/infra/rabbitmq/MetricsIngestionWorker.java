package com.asre.asre.infra.rabbitmq;

import com.asre.asre.application.ingestion.MetricIngestionService;
import com.asre.asre.domain.ingestion.Metric;
import com.asre.asre.domain.service.ServiceDiscoveryPort;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class MetricsIngestionWorker {

    private final MetricIngestionService metricIngestionService;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final ServiceDiscoveryPort serviceDiscoveryPort;

    @Value("${ingestion.batch.size:100}")
    private int batchSize;

    @Value("${ingestion.batch.timeout-seconds:1}")
    private int timeoutSeconds;

    private final List<Metric> batchBuffer = new ArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private volatile boolean processing = false;

    @RabbitListener(queues = "metrics.ingest")
    public void handleMessage(String message) {
        try {
            // Parse message
            Map<String, Object> payload = objectMapper.readValue(message, new TypeReference<Map<String, Object>>() {
            });
            Metric metric = parseMetric(payload);

            if (metric == null || !metric.isValid()) {
                log.warn("Invalid metric received, sending to DLQ");
                sendToDlq(message, "metrics.dlq");
                return;
            }

            synchronized (batchBuffer) {
                batchBuffer.add(metric);

                if (batchBuffer.size() >= batchSize) {
                    processBatch();
                } else if (!processing) {
                    // Schedule timeout processing
                    processing = true;
                    scheduler.schedule(this::processBatchOnTimeout, timeoutSeconds, TimeUnit.SECONDS);
                }
            }
        } catch (Exception e) {
            log.error("Error processing metric message", e);
            sendToDlq(message, "metrics.dlq");
        }
    }

    private void processBatch() {
        List<Metric> batch;
        synchronized (batchBuffer) {
            if (batchBuffer.isEmpty()) {
                processing = false;
                return;
            }
            batch = new ArrayList<>(batchBuffer);
            batchBuffer.clear();
            processing = false;
        }

        try {
            metricIngestionService.ingestBatch(batch);
        } catch (Exception e) {
            log.error("Error ingesting metrics batch", e);
            // Send to DLQ - in production, you might want to retry
            batch.forEach(metric -> {
                try {
                    sendToDlq(objectMapper.writeValueAsString(metric), "metrics.dlq");
                } catch (Exception ex) {
                    log.error("Error sending metric to DLQ", ex);
                }
            });
        }
    }

    private void processBatchOnTimeout() {
        processBatch();
    }

    private Metric parseMetric(Map<String, Object> payload) {
        try {
            Metric metric = new Metric();
            UUID projectId = UUID.fromString((String) payload.get("project_id"));
            metric.setProjectId(projectId);

            // Handle serviceId - either from payload or discover by name
            UUID serviceId = null;
            if (payload.containsKey("service_id") && payload.get("service_id") != null) {
                serviceId = UUID.fromString((String) payload.get("service_id"));
            } else {
                // Discover service by name (or use default)
                String serviceName = payload.containsKey("service_name")
                        ? (String) payload.get("service_name")
                        : "default-service";
                try {
                    var service = serviceDiscoveryPort.discoverService(projectId, null, serviceName);
                    serviceId = service.getId();
                } catch (Exception e) {
                    log.error("Failed to discover service '{}' for project {}, cannot proceed", serviceName, projectId,
                            e);
                    // Return null so it goes to DLQ - service discovery must succeed
                    return null;
                }
            }
            metric.setServiceId(serviceId);

            metric.setMetricName((String) payload.get("metric_name"));
            metric.setValue(((Number) payload.get("value")).doubleValue());

            String timestampStr = (String) payload.get("timestamp");
            metric.setTimestamp(Instant.parse(timestampStr));

            @SuppressWarnings("unchecked")
            Map<String, String> tags = (Map<String, String>) payload.get("tags");
            metric.setTags(tags);

            return metric;
        } catch (Exception e) {
            log.error("Error parsing metric from payload", e);
            return null;
        }
    }

    private void sendToDlq(String message, String dlqName) {
        try {
            rabbitTemplate.convertAndSend(dlqName, message);
        } catch (Exception e) {
            log.error("Error sending message to DLQ: {}", dlqName, e);
        }
    }
}
