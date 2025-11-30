package com.asre.asre.domain.metrics;

import lombok.Value;

/**
 * Value object representing a single bin in a histogram.
 */
@Value
public class HistogramBin {
    double minValue;
    double maxValue;
    long count;

    public HistogramBin(double minValue, double maxValue, long count) {
        if (minValue > maxValue) {
            throw new IllegalArgumentException("Min value cannot be greater than max value");
        }
        if (count < 0) {
            throw new IllegalArgumentException("Count cannot be negative");
        }
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.count = count;
    }
}

