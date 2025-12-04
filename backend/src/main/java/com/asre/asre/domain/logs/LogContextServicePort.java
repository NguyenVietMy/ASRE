package com.asre.asre.domain.logs;

import com.asre.asre.domain.ingestion.LogEntry;

import java.util.List;

/**
 * Domain service for retrieving log context around a specific log entry.
 * Defines the meaning: "log context around event".
 * Repository executes the actual queries.
 */
public interface LogContextServicePort {
    /**
     * Get log context around a specific log entry.
     * Returns the target log plus surrounding logs (before and after).
     * 
     * @param logId The ID of the target log
     * @param projectId Project ID for isolation
     * @param beforeCount Number of logs before (domain-enforced limit)
     * @param afterCount Number of logs after (domain-enforced limit)
     * @return LogContext containing target log and surrounding logs
     */
    LogContext getContext(LogId logId, java.util.UUID projectId, int beforeCount, int afterCount);
    
    /**
     * Value object representing log context.
     */
    record LogContext(
            LogEntry targetLog,
            List<LogEntry> beforeLogs,
            List<LogEntry> afterLogs
    ) {
        public LogContext {
            if (targetLog == null) {
                throw new IllegalArgumentException("Target log cannot be null");
            }
            if (beforeLogs == null) {
                throw new IllegalArgumentException("Before logs cannot be null");
            }
            if (afterLogs == null) {
                throw new IllegalArgumentException("After logs cannot be null");
            }
        }
        
        /**
         * Get all logs in chronological order (before, target, after).
         */
        public List<LogEntry> getAllLogsInOrder() {
            java.util.List<LogEntry> all = new java.util.ArrayList<>(beforeLogs);
            all.add(targetLog);
            all.addAll(afterLogs);
            return List.copyOf(all);
        }
    }
}

