package com.asre.asre.domain.metrics;

import lombok.Value;

import java.util.List;

/**
 * Domain object representing the result of a histogram query.
 */
@Value
public class HistogramResult {
    String metricName;
    List<HistogramBin> bins;

    public HistogramResult(String metricName, List<HistogramBin> bins) {
        if (metricName == null || metricName.isBlank()) {
            throw new IllegalArgumentException("Metric name cannot be null or blank");
        }
        if (bins == null) {
            throw new IllegalArgumentException("Bins cannot be null");
        }
        this.metricName = metricName;
        this.bins = List.copyOf(bins);
    }
}

