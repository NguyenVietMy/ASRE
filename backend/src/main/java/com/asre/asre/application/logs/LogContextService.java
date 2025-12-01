package com.asre.asre.application.logs;

import com.asre.asre.domain.logs.LogContextPolicy;
import com.asre.asre.domain.logs.LogId;
import com.asre.asre.domain.logs.LogContextService.LogContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Application service wrapper for log context retrieval.
 * Delegates to infrastructure implementation.
 */
@Service
@RequiredArgsConstructor
public class LogContextService {

    private final com.asre.asre.domain.logs.LogContextService domainContextService;

    /**
     * Get log context around a specific log entry.
     * Default: 10 before + 10 after.
     */
    public LogContext getContext(LogId logId, UUID projectId) {
        return getContext(logId, projectId, 10, 10);
    }

    /**
     * Get log context with custom window sizes.
     */
    public LogContext getContext(LogId logId, UUID projectId, int beforeCount, int afterCount) {
        // Enforce domain policy before calling infrastructure
        LogContextPolicy.validateContextWindow(beforeCount, afterCount);
        return domainContextService.getContext(logId, projectId, beforeCount, afterCount);
    }
}

