package com.asre.asre.domain.logs;

import com.asre.asre.domain.ingestion.LogEntry;
import lombok.Value;

import java.util.List;

/**
 * Specialized result type for trace queries.
 * Makes intent clear: this is a trace view, not random search results.
 */
@Value
public class TraceLogResult {
    String traceId;
    List<LogEntry> logsInOrder; // Sorted by timestamp for timeline view

    public TraceLogResult(String traceId, List<LogEntry> logsInOrder) {
        if (traceId == null || traceId.isBlank()) {
            throw new IllegalArgumentException("Trace ID cannot be null or blank");
        }
        if (logsInOrder == null) {
            throw new IllegalArgumentException("Logs list cannot be null");
        }
        this.traceId = traceId;
        this.logsInOrder = List.copyOf(logsInOrder);
    }
}


