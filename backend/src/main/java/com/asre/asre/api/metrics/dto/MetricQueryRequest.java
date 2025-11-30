package com.asre.asre.api.metrics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricQueryRequest {
    @NotBlank
    private String metric;

    @NotBlank
    private String stat; // avg, min, max, p50, p95, p99, sum, count

    @NotBlank
    private String rollup; // 1m, 5m, 1h, 1d

    @NotNull
    private String startTime; // ISO timestamp

    @NotNull
    private String endTime; // ISO timestamp

    private UUID serviceId; // Optional
}

