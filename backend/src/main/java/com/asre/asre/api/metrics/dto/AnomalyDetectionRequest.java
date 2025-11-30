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
public class AnomalyDetectionRequest {
    @NotBlank
    private String metric;

    @NotNull
    private String startTime;

    @NotNull
    private String endTime;

    @NotBlank
    private String method; // zscore, isolation_forest

    private UUID serviceId;
}

