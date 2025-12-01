package com.asre.asre.domain.logs;

import lombok.Value;

/**
 * Value object representing sort order for log queries.
 */
@Value
public class LogSortOrder {
    LogSortField field;
    SortDirection direction;

    public LogSortOrder(LogSortField field, SortDirection direction) {
        if (field == null) {
            throw new IllegalArgumentException("Sort field cannot be null");
        }
        if (direction == null) {
            throw new IllegalArgumentException("Sort direction cannot be null");
        }
        this.field = field;
        this.direction = direction;
    }

    /**
     * Default sort: timestamp descending (newest first).
     */
    public static LogSortOrder defaultOrder() {
        return new LogSortOrder(LogSortField.TIMESTAMP, SortDirection.DESC);
    }

    /**
     * Sort by ingested_at descending (most recently ingested first).
     */
    public static LogSortOrder byIngestedAtDesc() {
        return new LogSortOrder(LogSortField.INGESTED_AT, SortDirection.DESC);
    }

    public enum LogSortField {
        TIMESTAMP,
        INGESTED_AT
    }

    public enum SortDirection {
        ASC,
        DESC
    }
}


