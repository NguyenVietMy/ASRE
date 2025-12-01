package com.asre.asre.domain.logs;

import lombok.Value;

import java.util.UUID;

/**
 * First-class domain concept for trace-based log queries.
 * Not just another filter - implies special semantics (timeline, all services, etc.)
 */
@Value
public class TraceLogQuery {
    UUID projectId;
    String traceId;

    public TraceLogQuery(UUID projectId, String traceId) {
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }
        if (traceId == null || traceId.isBlank()) {
            throw new IllegalArgumentException("Trace ID cannot be null or blank");
        }
        this.projectId = projectId;
        this.traceId = traceId.trim();
    }
}


