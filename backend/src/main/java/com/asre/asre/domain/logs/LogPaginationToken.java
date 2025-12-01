package com.asre.asre.domain.logs;

import lombok.Value;

/**
 * Domain value object for log query pagination.
 * Infrastructure layer maps this to/from OpenSearch search_after.
 * Domain doesn't know about search_after mechanics.
 */
@Value
public class LogPaginationToken {
    String value;

    public LogPaginationToken(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Pagination token cannot be null or blank");
        }
        this.value = value;
    }

    /**
     * Create a token from a serialized representation.
     * Used by infrastructure to deserialize from storage.
     */
    public static LogPaginationToken fromString(String value) {
        return new LogPaginationToken(value);
    }
}


