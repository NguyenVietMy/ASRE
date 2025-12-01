package com.asre.asre.api.logs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogQueryResponse {
    private List<LogEntry> logs;
    private String nextPageToken; // Pagination token for next page
    private int totalReturned;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogEntry {
        private String logId;
        private String projectId;
        private String serviceId;
        private String level;
        private String message;
        private String timestamp;
        private String traceId;
        private Map<String, Object> context;
        private boolean sampled;
    }
}


