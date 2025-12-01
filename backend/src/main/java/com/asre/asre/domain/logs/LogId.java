package com.asre.asre.domain.logs;

import lombok.Value;

/**
 * Domain identifier for log entries.
 * Abstracts away OpenSearch _id or other storage-specific identifiers.
 */
@Value
public class LogId {
    String value;

    public LogId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Log ID cannot be null or blank");
        }
        this.value = value;
    }
}


