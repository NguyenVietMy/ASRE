package com.asre.asre.application.ingestion;

import com.asre.asre.domain.ingestion.IngestLogsCommand;
import com.asre.asre.domain.ingestion.IngestMetricsCommand;
import com.asre.asre.domain.ingestion.MessagePublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class IngestionService {

    private final MessagePublisherPort messagePublisher;
    private final ObjectMapper objectMapper;

    public void ingestMetrics(IngestMetricsCommand command) {
        try {
            for (var metric : command.getMetrics()) {
                Map<String, Object> message = new HashMap<>();
                message.put("project_id", command.getProjectId().toString());
                message.put("service_id", metric.getServiceId().toString());
                message.put("metric_name", metric.getMetricName());
                message.put("value", metric.getValue());
                message.put("timestamp", metric.getTimestamp());
                if (metric.getTags() != null) {
                    message.put("tags", metric.getTags());
                }

                messagePublisher.publishMetricsMessage("metrics.ingest", objectMapper.writeValueAsString(message));
            }
        } catch (Exception e) {
            log.error("Error enqueueing metrics", e);
            throw new RuntimeException("Failed to enqueue metrics", e);
        }
    }

    public void ingestLogs(IngestLogsCommand command) {
        try {
            for (var log : command.getLogs()) {
                Map<String, Object> message = new HashMap<>();
                message.put("project_id", command.getProjectId().toString());
                message.put("service_id", log.getServiceId().toString());
                message.put("level", log.getLevel());
                message.put("message", log.getMessage());
                message.put("timestamp", log.getTimestamp());
                if (log.getTraceId() != null) {
                    message.put("trace_id", log.getTraceId());
                }
                if (log.getContext() != null) {
                    message.put("context", log.getContext());
                }

                messagePublisher.publishLogsMessage("logs.ingest", objectMapper.writeValueAsString(message));
            }
        } catch (Exception e) {
            log.error("Error enqueueing logs", e);
            throw new RuntimeException("Failed to enqueue logs", e);
        }
    }
}
