package com.asre.asre.domain.metrics;

import lombok.Value;

import java.util.List;

/**
 * Domain object representing the result of a metric query.
 */
@Value
public class MetricQueryResult {
    String metricName;
    AggregationType aggregationType;
    List<TimeSeriesPoint> dataPoints;

    public MetricQueryResult(String metricName, AggregationType aggregationType, List<TimeSeriesPoint> dataPoints) {
        if (metricName == null || metricName.isBlank()) {
            throw new IllegalArgumentException("Metric name cannot be null or blank");
        }
        if (aggregationType == null) {
            throw new IllegalArgumentException("Aggregation type cannot be null");
        }
        if (dataPoints == null) {
            throw new IllegalArgumentException("Data points cannot be null");
        }
        this.metricName = metricName;
        this.aggregationType = aggregationType;
        this.dataPoints = List.copyOf(dataPoints); // Immutable copy
    }
}

