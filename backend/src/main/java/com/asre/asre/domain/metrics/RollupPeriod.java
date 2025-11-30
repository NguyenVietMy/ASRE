package com.asre.asre.domain.metrics;

import lombok.Value;

import java.time.Duration;

/**
 * Value object representing a rollup period for time-series aggregation.
 */
@Value
public class RollupPeriod {
    Duration duration;

    public RollupPeriod(String period) {
        if (period == null || period.isBlank()) {
            throw new IllegalArgumentException("Rollup period cannot be null or blank");
        }
        this.duration = parseDuration(period);
    }

    public RollupPeriod(Duration duration) {
        if (duration == null || duration.isNegative() || duration.isZero()) {
            throw new IllegalArgumentException("Rollup duration must be positive");
        }
        this.duration = duration;
    }

    private Duration parseDuration(String period) {
        String trimmed = period.trim().toLowerCase();
        if (trimmed.endsWith("m")) {
            int minutes = Integer.parseInt(trimmed.substring(0, trimmed.length() - 1));
            return Duration.ofMinutes(minutes);
        } else if (trimmed.endsWith("h")) {
            int hours = Integer.parseInt(trimmed.substring(0, trimmed.length() - 1));
            return Duration.ofHours(hours);
        } else if (trimmed.endsWith("d")) {
            int days = Integer.parseInt(trimmed.substring(0, trimmed.length() - 1));
            return Duration.ofDays(days);
        } else {
            throw new IllegalArgumentException("Invalid rollup period format: " + period + ". Expected format: 1m, 5m, 1h, 1d");
        }
    }

    public String toPeriodString() {
        long minutes = duration.toMinutes();
        if (minutes < 60) {
            return minutes + "m";
        }
        long hours = duration.toHours();
        if (hours < 24) {
            return hours + "h";
        }
        long days = duration.toDays();
        return days + "d";
    }

    public static RollupPeriod ONE_MINUTE = new RollupPeriod(Duration.ofMinutes(1));
    public static RollupPeriod FIVE_MINUTES = new RollupPeriod(Duration.ofMinutes(5));
    public static RollupPeriod ONE_HOUR = new RollupPeriod(Duration.ofHours(1));
    public static RollupPeriod ONE_DAY = new RollupPeriod(Duration.ofDays(1));
}

