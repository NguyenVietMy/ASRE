package com.asre.asre.api.metrics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyDetectionResponse {
    private List<AnomalyResult> anomalies;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnomalyResult {
        private String timestamp;
        private Double zScore;
        private Boolean isAnomaly;
        private Double value;
    }
}
