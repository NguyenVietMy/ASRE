package com.asre.asre.api.ingestion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricsIngestionRequest {
    @NotEmpty
    @Valid
    private List<MetricDto> metrics;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricDto {
        private String name;
        private Double value;
        private UUID serviceId;
        private String timestamp;
        private Map<String, String> tags;
    }
}


