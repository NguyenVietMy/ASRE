package com.asre.asre.domain.ingestion;

import java.util.List;

/**
 * Port for indexing logs to search engine.
 * Implementation will be in infrastructure layer (OpenSearch).
 */
public interface LogIndexer {
    void indexLog(LogEntry logEntry, String logId);
    void indexBatch(List<LogEntry> logEntries);
}

