package com.asre.asre.domain.metrics;

/**
 * Enumeration of supported aggregation types for metrics.
 */
public enum AggregationType {
    AVG("avg"),
    MIN("min"),
    MAX("max"),
    P50("p50"),
    P95("p95"),
    P99("p99"),
    SUM("sum"),
    COUNT("count");

    private final String value;

    AggregationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static AggregationType fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Aggregation type cannot be null");
        }
        for (AggregationType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown aggregation type: " + value);
    }

    /**
     * Returns true if this is a percentile aggregation.
     */
    public boolean isPercentile() {
        return this == P50 || this == P95 || this == P99;
    }
}

