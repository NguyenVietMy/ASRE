package com.asre.asre.domain.ingestion;

import lombok.Value;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Value
public class IngestLogsCommand {
    UUID projectId;
    List<LogData> logs;

    @Value
    public static class LogData {
        UUID serviceId;
        String level;
        String message;
        String timestamp;
        String traceId;
        Map<String, Object> context;
    }
}


