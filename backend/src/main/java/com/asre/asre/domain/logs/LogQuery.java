package com.asre.asre.domain.logs;

import com.asre.asre.domain.metrics.TimeRange;
import lombok.Value;

import java.util.Optional;
import java.util.UUID;

/**
 * Domain value object representing a log search query.
 * Encapsulates all parameters needed to query logs.
 */
@Value
public class LogQuery {
    UUID projectId;
    TimeRange timeRange;
    Optional<ServiceFilter> serviceFilter;
    Optional<LogLevelFilter> levelFilter;
    Optional<String> searchText; // Full-text search
    Optional<String> traceId;
    LogSortOrder sortOrder;
    int limit;
    Optional<LogPaginationToken> paginationToken;

    public LogQuery(
            UUID projectId,
            TimeRange timeRange,
            ServiceFilter serviceFilter,
            LogLevelFilter levelFilter,
            String searchText,
            String traceId,
            LogSortOrder sortOrder,
            int limit,
            LogPaginationToken paginationToken) {
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }
        if (timeRange == null) {
            throw new IllegalArgumentException("Time range cannot be null");
        }
        if (sortOrder == null) {
            throw new IllegalArgumentException("Sort order cannot be null");
        }
        if (limit <= 0 || limit > 10000) {
            throw new IllegalArgumentException("Limit must be between 1 and 10000");
        }
        
        this.projectId = projectId;
        this.timeRange = timeRange;
        this.serviceFilter = Optional.ofNullable(serviceFilter);
        this.levelFilter = Optional.ofNullable(levelFilter);
        this.searchText = Optional.ofNullable(searchText != null && !searchText.isBlank() ? searchText.trim() : null);
        this.traceId = Optional.ofNullable(traceId != null && !traceId.isBlank() ? traceId.trim() : null);
        this.sortOrder = sortOrder;
        this.limit = limit;
        this.paginationToken = Optional.ofNullable(paginationToken);
    }

    /**
     * Create a simple query with defaults.
     */
    public static LogQuery simple(UUID projectId, TimeRange timeRange, int limit) {
        return new LogQuery(
                projectId,
                timeRange,
                null,
                null,
                null,
                null,
                LogSortOrder.defaultOrder(),
                limit,
                null
        );
    }
}


