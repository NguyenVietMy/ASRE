package com.asre.asre.api.logs.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogQueryRequest {
    @NotNull
    private String startTime; // ISO timestamp

    @NotNull
    private String endTime; // ISO timestamp

    private String level; // Single level or comma-separated (e.g., "ERROR" or "ERROR,WARN")

    private UUID serviceId;

    private String search; // Full-text search

    private String traceId;

    private String sort; // "asc" or "desc"

    private String sortField; // "timestamp" or "ingested_at"

    @Min(1)
    @Max(10000)
    private int limit = 100;

    private String searchAfter; // Pagination token
}


