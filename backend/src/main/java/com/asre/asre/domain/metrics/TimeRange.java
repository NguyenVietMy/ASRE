package com.asre.asre.domain.metrics;

import lombok.Value;

import java.time.Instant;

/**
 * Value object representing a time range for metric queries.
 */
@Value
public class TimeRange {
    Instant startTime;
    Instant endTime;

    public TimeRange(Instant startTime, Instant endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start time and end time cannot be null");
        }
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Start time must be before or equal to end time");
        }
        if (startTime.isAfter(Instant.now())) {
            throw new IllegalArgumentException("Start time cannot be in the future");
        }
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * Returns the duration in seconds.
     */
    public long getDurationSeconds() {
        return endTime.getEpochSecond() - startTime.getEpochSecond();
    }

    /**
     * Validates that the time range is within reasonable bounds.
     */
    public void validateBounds(long maxDurationDays) {
        long durationDays = getDurationSeconds() / (24 * 60 * 60);
        if (durationDays > maxDurationDays) {
            throw new IllegalArgumentException(
                    String.format("Time range exceeds maximum allowed duration of %d days", maxDurationDays));
        }
    }
}

