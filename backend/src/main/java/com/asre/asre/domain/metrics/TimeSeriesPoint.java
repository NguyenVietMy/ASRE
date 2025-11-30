package com.asre.asre.domain.metrics;

import lombok.Value;

import java.time.Instant;

/**
 * Value object representing a single point in a time series.
 */
@Value
public class TimeSeriesPoint {
    Instant timestamp;
    Double value;

    public TimeSeriesPoint(Instant timestamp, Double value) {
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp cannot be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        this.timestamp = timestamp;
        this.value = value;
    }
}

