package com.asre.asre.api.logs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TraceLogResponse {
    private String traceId;
    private List<LogQueryResponse.LogEntry> logs; // Reuse LogEntry from LogQueryResponse
}


