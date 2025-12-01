package com.asre.asre.application.logs;

import com.asre.asre.domain.logs.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Application service for log volume aggregations and error spike detection.
 * Orchestrates domain services and repositories.
 * Domain interprets spikes, infrastructure returns raw data.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LogVolumeService {

    private final LogVolumeRepository volumeRepository;
    private final LogCache cache;
    private final LogQueryValidator queryValidator = new LogQueryValidator(); // Domain service, no dependencies
    private final ErrorSpikeDetector spikeDetector = new ErrorSpikeDetector(); // Domain service, no dependencies

    /**
     * Execute a log volume aggregation query with caching.
     */
    public LogVolumeResult queryVolume(LogVolumeQuery query) {
        // Validate query
        queryValidator.validate(query);

        // Try cache first
        return cache.getVolume(query)
                .orElseGet(() -> {
                    // Cache miss - execute query
                    LogVolumeResult result = volumeRepository.executeVolumeQuery(query);
                    // Cache the result
                    cache.putVolume(query, result);
                    return result;
                });
    }

    /**
     * Execute error spike detection query.
     * Domain interprets spikes from raw data buckets.
     */
    public ErrorSpikeResult detectErrorSpikes(ErrorSpikeQuery query) {
        // Validate query
        queryValidator.validate(query);

        // Try cache first
        return cache.getErrorSpikes(query)
                .orElseGet(() -> {
                    // Cache miss - get raw volume data (ERROR level only)
                    LogVolumeQuery volumeQuery = new LogVolumeQuery(
                            query.getProjectId(),
                            query.getTimeRange(),
                            query.getRollupPeriod(),
                            query.getServiceFilter().orElse(null),
                            LogLevelFilter.errorsOnly() // ERROR and FATAL only
                    );
                    
                    LogVolumeResult volumeResult = volumeRepository.executeVolumeQuery(volumeQuery);
                    List<LogVolumePoint> rawDataPoints = volumeResult.getDataPoints();

                    // Domain interprets spikes (threshold, severity, descriptions)
                    ErrorSpikeResult result = spikeDetector.detectSpikes(
                            rawDataPoints,
                            query.getSpikeThreshold().orElse(null)
                    );

                    // Cache the interpreted result
                    cache.putErrorSpikes(query, result);
                    return result;
                });
    }
}

