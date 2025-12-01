package com.asre.asre.domain.ingestion;

import com.asre.asre.domain.logs.LogId;
import com.asre.asre.domain.logs.LogLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogEntry {
    private LogId logId; // Domain identifier
    private UUID projectId;
    private UUID serviceId;
    private LogLevel level; // Domain enum instead of string
    private String message;
    private Instant timestamp;
    private String traceId;
    private Map<String, Object> context;
    private boolean sampled; // Whether this log was sampled

    public LogEntry(UUID projectId, UUID serviceId, LogLevel level, String message, 
                   Instant timestamp, String traceId, Map<String, Object> context) {
        this(null, projectId, serviceId, level, message, timestamp, traceId, context, false);
    }

    public boolean isValid() {
        return projectId != null
                && serviceId != null
                && level != null
                && message != null
                && timestamp != null;
    }

    /**
     * Get log ID, generating one if not set.
     * Used when creating logs that don't have an ID yet.
     */
    public LogId getOrCreateLogId() {
        if (logId == null) {
            logId = new LogId(java.util.UUID.randomUUID().toString());
        }
        return logId;
    }
}
