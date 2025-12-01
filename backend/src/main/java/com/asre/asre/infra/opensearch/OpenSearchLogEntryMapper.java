package com.asre.asre.infra.opensearch;

import com.asre.asre.domain.ingestion.LogEntry;
import com.asre.asre.domain.logs.LogId;
import com.asre.asre.domain.logs.LogLevel;
import org.opensearch.search.SearchHit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Mapper for converting OpenSearch SearchHit to domain LogEntry.
 * Centralizes parsing logic to avoid duplication.
 */
public class OpenSearchLogEntryMapper {

    /**
     * Parse a single SearchHit to LogEntry.
     */
    public static LogEntry parseLogEntry(SearchHit hit) {
        Map<String, Object> source = hit.getSourceAsMap();
        
        LogId logId = new LogId(hit.getId());
        UUID projectId = UUID.fromString((String) source.get("project_id"));
        UUID serviceId = UUID.fromString((String) source.get("service_id"));
        LogLevel level = LogLevel.fromString((String) source.get("level"));
        String message = (String) source.get("message");
        java.time.Instant timestamp = java.time.Instant.parse((String) source.get("timestamp"));
        String traceId = (String) source.get("trace_id");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> context = (Map<String, Object>) source.get("context");
        
        boolean sampled = source.containsKey("sampled") && 
                Boolean.TRUE.equals(source.get("sampled"));

        return new LogEntry(
                logId,
                projectId,
                serviceId,
                level,
                message,
                timestamp,
                traceId,
                context,
                sampled
        );
    }

    /**
     * Parse multiple SearchHits to List<LogEntry>.
     */
    public static List<LogEntry> parseLogEntries(SearchHit[] hits) {
        List<LogEntry> logs = new ArrayList<>();
        for (SearchHit hit : hits) {
            logs.add(parseLogEntry(hit));
        }
        return logs;
    }
}

