package com.asre.asre.domain.metrics;

import lombok.Value;

import java.util.List;

/**
 * Domain object representing the result of a multi-metric query.
 */
@Value
public class MultiMetricQueryResult {
    List<MetricQueryResult> results;

    public MultiMetricQueryResult(List<MetricQueryResult> results) {
        if (results == null) {
            throw new IllegalArgumentException("Results cannot be null");
        }
        this.results = List.copyOf(results);
    }
}

