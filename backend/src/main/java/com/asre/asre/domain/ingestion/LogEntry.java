package com.asre.asre.domain.ingestion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogEntry {
    private UUID projectId;
    private UUID serviceId;
    private String level;
    private String message;
    private Instant timestamp;
    private String traceId;
    private Map<String, Object> context;

    public boolean isValid() {
        return projectId != null
                && serviceId != null
                && level != null && !level.isBlank()
                && message != null
                && timestamp != null;
    }
}
