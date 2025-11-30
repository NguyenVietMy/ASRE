package com.asre.asre.application.metrics;

import com.asre.asre.domain.metrics.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Application service for metrics querying.
 * Orchestrates domain services and repositories.
 * Transport-agnostic and persistence-agnostic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsQueryService {

    private final MetricQueryRepository queryRepository;
    private final MetricCache cache;
    private final AnomalyDetector anomalyDetector;
    private final AnomalyDetectionResultRepository anomalyRepository;

    /**
     * Execute a single metric query with caching.
     */
    public MetricQueryResult queryMetrics(MetricQuery query) {
        // Validate query at domain level
        query.getTimeRange().validateBounds(90); // Max 90 days

        // Try cache first - cache handles key generation internally
        return cache.get(query)
                .orElseGet(() -> {
                    // Cache miss - execute query
                    MetricQueryResult result = queryRepository.executeQuery(query);
                    // Cache the result - TTL calculation handled by cache implementation
                    cache.put(query, result);
                    return result;
                });
    }

    /**
     * Execute a multi-metric query (overlay) with caching.
     */
    public MultiMetricQueryResult queryMultipleMetrics(MultiMetricQuery query) {
        // Validate query
        query.getTimeRange().validateBounds(90);

        return cache.getMulti(query)
                .orElseGet(() -> {
                    MultiMetricQueryResult result = queryRepository.executeMultiQuery(query);
                    cache.putMulti(query, result);
                    return result;
                });
    }

    /**
     * Execute a histogram query with caching.
     */
    public HistogramResult queryHistogram(HistogramQuery query) {
        query.getTimeRange().validateBounds(30); // Histograms limited to 30 days

        return cache.getHistogram(query)
                .orElseGet(() -> {
                    HistogramResult result = queryRepository.executeHistogramQuery(query);
                    cache.putHistogram(query, result);
                    return result;
                });
    }

    /**
     * Execute anomaly detection query.
     * First queries the metrics, then runs anomaly detection, then stores results.
     */
    public List<AnomalyDetectionResult> detectAnomalies(AnomalyDetectionQuery query) {
        query.getTimeRange().validateBounds(30); // Anomaly detection limited to 30 days

        // First, get the time series data
        MetricQuery baseQuery = new MetricQuery(
                query.getProjectId(),
                query.getMetricName(),
                AggregationType.AVG, // Use AVG for anomaly detection baseline
                query.getTimeRange(),
                RollupPeriod.ONE_MINUTE, // 1-minute granularity for anomaly detection
                query.getServiceId().orElse(null),
                null);

        MetricQueryResult timeSeries = queryRepository.executeQuery(baseQuery);

        // Run anomaly detection
        // serviceId can be null for project-wide anomaly detection
        List<AnomalyDetectionResult> results = anomalyDetector.detectAnomalies(
                timeSeries.getDataPoints(),
                query.getMethod(),
                query.getProjectId(),
                query.getServiceId().orElse(null),
                query.getMetricName());

        // Store results
        results.forEach(anomalyRepository::save);

        return results;
    }
}
