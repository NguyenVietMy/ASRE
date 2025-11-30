package com.asre.asre.api.metrics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricQueryResponse {
    private String metric;
    private String stat;
    private List<DataPoint> data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataPoint {
        private String timestamp;
        private Double value;
    }
}

