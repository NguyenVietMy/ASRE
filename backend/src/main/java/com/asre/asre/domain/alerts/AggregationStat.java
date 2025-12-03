package com.asre.asre.domain.alerts;

/**
 * Aggregation statistics for alert rule evaluation.
 * Maps to metric aggregation types.
 */
public enum AggregationStat {
    AVG,
    P95,
    P99,
    MAX,
    MIN;

    public static AggregationStat fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Aggregation stat cannot be null");
        }
        for (AggregationStat stat : values()) {
            if (stat.name().equalsIgnoreCase(value)) {
                return stat;
            }
        }
        throw new IllegalArgumentException("Unknown aggregation stat: " + value);
    }
}

