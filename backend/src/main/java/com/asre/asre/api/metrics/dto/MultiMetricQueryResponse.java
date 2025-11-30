package com.asre.asre.api.metrics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultiMetricQueryResponse {
    private List<MetricQueryResponse> results;
}

