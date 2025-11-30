package com.asre.asre.api.metrics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistogramResponse {
    private String metric;
    private List<Bin> bins;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Bin {
        private Double minValue;
        private Double maxValue;
        private Long count;
    }
}

