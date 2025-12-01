package com.asre.asre.domain.logs;

import com.asre.asre.domain.ingestion.LogEntry;
import lombok.Value;

import java.util.List;
import java.util.Optional;

/**
 * Domain object representing the result of a log query.
 * Contains only meaningful domain data, no technical details.
 */
@Value
public class LogQueryResult {
    List<LogEntry> logs;
    Optional<LogPaginationToken> nextPageToken;
    int totalReturned;

    public LogQueryResult(List<LogEntry> logs, LogPaginationToken nextPageToken) {
        if (logs == null) {
            throw new IllegalArgumentException("Logs list cannot be null");
        }
        this.logs = List.copyOf(logs);
        this.nextPageToken = Optional.ofNullable(nextPageToken);
        this.totalReturned = logs.size();
    }

    public LogQueryResult(List<LogEntry> logs) {
        this(logs, null);
    }

    /**
     * Returns true if there are more results available.
     */
    public boolean hasMore() {
        return nextPageToken.isPresent();
    }
}

