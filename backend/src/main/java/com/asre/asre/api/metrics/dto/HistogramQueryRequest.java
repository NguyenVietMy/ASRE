package com.asre.asre.api.metrics.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistogramQueryRequest {
    @NotBlank
    private String metric;

    @Min(2)
    @Max(100)
    private int bins = 20;

    @NotNull
    private String startTime;

    @NotNull
    private String endTime;

    private UUID serviceId;
}

