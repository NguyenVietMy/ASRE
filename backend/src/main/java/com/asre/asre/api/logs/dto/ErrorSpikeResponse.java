package com.asre.asre.api.logs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorSpikeResponse {
    private List<Spike> spikes;
    private List<DataPoint> allDataPoints; // Raw buckets for UI

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Spike {
        private String timestamp;
        private Long errorCount;
        private Double spikeSeverity;
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataPoint {
        private String timestamp;
        private Long count;
    }
}


