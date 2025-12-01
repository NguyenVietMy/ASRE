package com.asre.asre.application.logs;

import com.asre.asre.domain.logs.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Application service for log querying.
 * Orchestrates domain services and repositories.
 * Transport-agnostic and persistence-agnostic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LogQueryService {

    private final LogQueryRepository queryRepository;
    private final LogQueryValidator queryValidator = new LogQueryValidator(); // Domain service, no dependencies

    /**
     * Execute a log search query.
     */
    public LogQueryResult queryLogs(LogQuery query) {
        // Validate query at domain level
        queryValidator.validate(query);

        // Execute query
        return queryRepository.executeQuery(query);
    }

    /**
     * Find a log by ID.
     */
    public com.asre.asre.domain.ingestion.LogEntry findLogById(LogId logId, UUID projectId) {
        return queryRepository.findById(logId, projectId);
    }

    /**
     * Execute a trace-based log query.
     */
    public TraceLogResult queryTraceLogs(TraceLogQuery query) {
        queryValidator.validate(query);
        return queryRepository.executeTraceQuery(query);
    }
}

