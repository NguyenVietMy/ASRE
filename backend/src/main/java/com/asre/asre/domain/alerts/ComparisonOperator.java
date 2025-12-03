package com.asre.asre.domain.alerts;

/**
 * Comparison operators for alert rule conditions.
 */
public enum ComparisonOperator {
    GT(">"),
    GTE(">="),
    LT("<"),
    LTE("<="),
    EQ("=="),
    NEQ("!=");

    private final String symbol;

    ComparisonOperator(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public static ComparisonOperator fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Comparison operator cannot be null");
        }
        for (ComparisonOperator op : values()) {
            if (op.symbol.equals(value) || op.name().equalsIgnoreCase(value)) {
                return op;
            }
        }
        throw new IllegalArgumentException("Unknown comparison operator: " + value);
    }

    /**
     * Evaluates the condition: value operator threshold
     */
    public boolean evaluate(double value, double threshold) {
        return switch (this) {
            case GT -> value > threshold;
            case GTE -> value >= threshold;
            case LT -> value < threshold;
            case LTE -> value <= threshold;
            case EQ -> Math.abs(value - threshold) < 0.0001; // Floating point comparison
            case NEQ -> Math.abs(value - threshold) >= 0.0001;
        };
    }
}

