package com.asre.asre.domain.logs;

/**
 * Repository interface for querying logs.
 * Implementations in infrastructure layer use OpenSearch.
 * Domain doesn't know about OpenSearch specifics.
 */
public interface LogQueryRepository {
    /**
     * Execute a log search query.
     * Returns logs with pagination token for next page.
     */
    LogQueryResult executeQuery(LogQuery query);

    /**
     * Find a log by its ID.
     * @throws LogNotFoundException if log not found
     */
    com.asre.asre.domain.ingestion.LogEntry findById(LogId logId, java.util.UUID projectId);

    /**
     * Execute a trace-based log query.
     */
    TraceLogResult executeTraceQuery(TraceLogQuery query);
}


