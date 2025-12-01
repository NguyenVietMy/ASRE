package com.asre.asre.infra.rabbitmq;

import com.asre.asre.application.ingestion.LogIngestionService;
import com.asre.asre.domain.ingestion.LogEntry;
import com.asre.asre.domain.logs.LogId;
import com.asre.asre.domain.logs.LogLevel;
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
public class LogsIngestionWorker {

    private final LogIngestionService logIngestionService;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ingestion.batch.size:100}")
    private int batchSize;

    @Value("${ingestion.batch.timeout-seconds:1}")
    private int timeoutSeconds;

    private final List<LogEntry> batchBuffer = new ArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private volatile boolean processing = false;

    @RabbitListener(queues = "logs.ingest")
    public void handleMessage(String message) {
        try {
            // Parse message
            Map<String, Object> payload = objectMapper.readValue(message, new TypeReference<Map<String, Object>>() {});
            LogEntry logEntry = parseLogEntry(payload);

            if (logEntry == null || !logEntry.isValid()) {
                log.warn("Invalid log entry received, sending to DLQ");
                sendToDlq(message, "logs.dlq");
                return;
            }

            synchronized (batchBuffer) {
                batchBuffer.add(logEntry);

                if (batchBuffer.size() >= batchSize) {
                    processBatch();
                } else if (!processing) {
                    // Schedule timeout processing
                    processing = true;
                    scheduler.schedule(this::processBatchOnTimeout, timeoutSeconds, TimeUnit.SECONDS);
                }
            }
        } catch (Exception e) {
            log.error("Error processing log message", e);
            sendToDlq(message, "logs.dlq");
        }
    }

    private void processBatch() {
        List<LogEntry> batch;
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
            logIngestionService.ingestBatch(batch);
        } catch (Exception e) {
            log.error("Error ingesting logs batch", e);
            // Send to DLQ
            batch.forEach(logEntry -> {
                try {
                    sendToDlq(objectMapper.writeValueAsString(logEntry), "logs.dlq");
                } catch (Exception ex) {
                    log.error("Error sending log to DLQ", ex);
                }
            });
        }
    }

    private void processBatchOnTimeout() {
        processBatch();
    }

    private LogEntry parseLogEntry(Map<String, Object> payload) {
        try {
            UUID projectId = UUID.fromString((String) payload.get("project_id"));
            UUID serviceId = UUID.fromString((String) payload.get("service_id"));
            LogLevel level = LogLevel.fromString((String) payload.get("level"));
            String message = (String) payload.get("message");
            String timestampStr = (String) payload.get("timestamp");
            Instant timestamp = Instant.parse(timestampStr);
            String traceId = (String) payload.get("trace_id");

            @SuppressWarnings("unchecked")
            Map<String, Object> context = (Map<String, Object>) payload.get("context");

            // Create LogEntry with LogLevel enum
            LogEntry logEntry = new LogEntry(
                    null, // LogId will be set during ingestion
                    projectId,
                    serviceId,
                    level,
                    message,
                    timestamp,
                    traceId,
                    context,
                    false // sampled flag set during ingestion
            );

            return logEntry;
        } catch (Exception e) {
            log.error("Error parsing log entry from payload", e);
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

