package com.asre.asre.domain.logs;

import com.asre.asre.domain.metrics.TimeRange;

/**
 * Domain service for validating log queries.
 * Enforces performance limits and query complexity rules.
 * Pure domain logic - no Spring dependencies.
 */
public class LogQueryValidator {
    
    private static final long MAX_TIME_RANGE_DAYS = 7; // Max 7 days for general queries
    private static final long MAX_TIME_RANGE_DAYS_FULL_TEXT = 1; // Max 1 day for full-text search
    private static final int MAX_RESULTS = 10000;
    private static final int MIN_RESULTS = 1;

    /**
     * Validate a log query and throw domain exception if invalid.
     */
    public void validate(LogQuery query) throws InvalidLogQueryException {
        // Validate time range
        validateTimeRange(query.getTimeRange(), query.getSearchText().isPresent());
        
        // Validate limit
        if (query.getLimit() < MIN_RESULTS || query.getLimit() > MAX_RESULTS) {
            throw new InvalidLogQueryException(
                    String.format("Limit must be between %d and %d", MIN_RESULTS, MAX_RESULTS));
        }
        
        // Validate query complexity
        validateQueryComplexity(query);
    }

    /**
     * Validate time range based on query type.
     */
    private void validateTimeRange(TimeRange timeRange, boolean hasFullTextSearch) {
        long maxDays = hasFullTextSearch ? MAX_TIME_RANGE_DAYS_FULL_TEXT : MAX_TIME_RANGE_DAYS;
        timeRange.validateBounds(maxDays);
    }

    /**
     * Validate query complexity rules.
     * Reject combinations known to be expensive.
     */
    private void validateQueryComplexity(LogQuery query) throws InvalidLogQueryException {
        boolean hasFullTextSearch = query.getSearchText().isPresent();
        boolean hasServiceFilter = query.getServiceFilter().isPresent() && 
                                   query.getServiceFilter().get().requiresService();
        boolean hasLevelFilter = query.getLevelFilter().isPresent();
        boolean hasTraceId = query.getTraceId().isPresent();
        
        // Rule: Unbounded full-text search over large time range without filters is expensive
        if (hasFullTextSearch && !hasServiceFilter && !hasLevelFilter && !hasTraceId) {
            long days = query.getTimeRange().getDurationSeconds() / (24 * 60 * 60);
            if (days > MAX_TIME_RANGE_DAYS_FULL_TEXT) {
                throw new InvalidLogQueryException(
                        String.format("Full-text search without filters is limited to %d days. " +
                                "Add service, level, or trace filters to search longer ranges.",
                                MAX_TIME_RANGE_DAYS_FULL_TEXT));
            }
        }
        
        // Rule: Must have at least one filter for large time ranges
        long days = query.getTimeRange().getDurationSeconds() / (24 * 60 * 60);
        if (days > 3 && !hasServiceFilter && !hasLevelFilter && !hasTraceId && !hasFullTextSearch) {
            throw new InvalidLogQueryException(
                    "Queries over 3 days must include at least one filter (service, level, trace, or search text)");
        }
    }

    /**
     * Validate a volume query.
     */
    public void validate(LogVolumeQuery query) throws InvalidLogQueryException {
        // Volume queries can have longer time ranges
        query.getTimeRange().validateBounds(30); // Max 30 days for volume
    }

    /**
     * Validate an error spike query.
     */
    public void validate(ErrorSpikeQuery query) throws InvalidLogQueryException {
        query.getTimeRange().validateBounds(30); // Max 30 days for error spikes
    }

    /**
     * Validate a trace query.
     */
    public void validate(TraceLogQuery query) throws InvalidLogQueryException {
        // Trace queries are typically bounded by trace duration, no time range needed
        // But we could add validation if needed
    }
}

