package com.asre.asre.domain.logs;

import lombok.Value;

import java.time.Instant;
import java.util.Optional;

/**
 * Value object representing a single point in a log volume time series.
 */
@Value
public class LogVolumePoint {
    Instant timestamp;
    long count;
    Optional<Long> estimatedCount; // If sampling was applied

    public LogVolumePoint(Instant timestamp, long count) {
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp cannot be null");
        }
        if (count < 0) {
            throw new IllegalArgumentException("Count cannot be negative");
        }
        this.timestamp = timestamp;
        this.count = count;
        this.estimatedCount = Optional.empty();
    }

    public LogVolumePoint(Instant timestamp, long count, long estimatedCount) {
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp cannot be null");
        }
        if (count < 0) {
            throw new IllegalArgumentException("Count cannot be negative");
        }
        if (estimatedCount < 0) {
            throw new IllegalArgumentException("Estimated count cannot be negative");
        }
        this.timestamp = timestamp;
        this.count = count;
        this.estimatedCount = Optional.of(estimatedCount);
    }
}

