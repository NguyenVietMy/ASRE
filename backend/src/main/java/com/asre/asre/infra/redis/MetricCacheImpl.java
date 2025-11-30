package com.asre.asre.infra.redis;

import com.asre.asre.application.metrics.MetricCache;
import com.asre.asre.domain.metrics.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * Redis implementation of MetricCache.
 * Centralizes all cache key generation and TTL calculation in infrastructure layer.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MetricCacheImpl implements MetricCache {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CACHE_PREFIX = "metric:query:";
    private static final String MULTI_CACHE_PREFIX = "metric:multi:";
    private static final String HISTOGRAM_CACHE_PREFIX = "metric:histogram:";

    @Override
    public Optional<MetricQueryResult> get(MetricQuery query) {
        try {
            String cacheKey = generateCacheKey(query);
            String value = redisTemplate.opsForValue().get(cacheKey);
            if (value == null) {
                return Optional.empty();
            }
            MetricQueryResult result = objectMapper.readValue(value, MetricQueryResult.class);
            return Optional.of(result);
        } catch (Exception e) {
            log.warn("Error reading from cache for query: {}", query.getMetricName(), e);
            return Optional.empty();
        }
    }

    @Override
    public void put(MetricQuery query, MetricQueryResult result) {
        try {
            String cacheKey = generateCacheKey(query);
            int ttlSeconds = calculateCacheTtl(query.getRollupPeriod());
            String value = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(cacheKey, value, Duration.ofSeconds(ttlSeconds));
        } catch (Exception e) {
            log.warn("Error writing to cache for query: {}", query.getMetricName(), e);
        }
    }

    @Override
    public Optional<MultiMetricQueryResult> getMulti(MultiMetricQuery query) {
        try {
            String cacheKey = generateMultiCacheKey(query);
            String value = redisTemplate.opsForValue().get(cacheKey);
            if (value == null) {
                return Optional.empty();
            }
            MultiMetricQueryResult result = objectMapper.readValue(value, MultiMetricQueryResult.class);
            return Optional.of(result);
        } catch (Exception e) {
            log.warn("Error reading multi-metric cache", e);
            return Optional.empty();
        }
    }

    @Override
    public void putMulti(MultiMetricQuery query, MultiMetricQueryResult result) {
        try {
            String cacheKey = generateMultiCacheKey(query);
            int ttlSeconds = calculateCacheTtl(query.getRollupPeriod());
            String value = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(cacheKey, value, Duration.ofSeconds(ttlSeconds));
        } catch (Exception e) {
            log.warn("Error writing multi-metric cache", e);
        }
    }

    @Override
    public Optional<HistogramResult> getHistogram(HistogramQuery query) {
        try {
            String cacheKey = generateHistogramCacheKey(query);
            String value = redisTemplate.opsForValue().get(cacheKey);
            if (value == null) {
                return Optional.empty();
            }
            HistogramResult result = objectMapper.readValue(value, HistogramResult.class);
            return Optional.of(result);
        } catch (Exception e) {
            log.warn("Error reading histogram cache", e);
            return Optional.empty();
        }
    }

    @Override
    public void putHistogram(HistogramQuery query, HistogramResult result) {
        try {
            String cacheKey = generateHistogramCacheKey(query);
            // Cache histograms for 5 minutes
            int ttlSeconds = 300;
            String value = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(cacheKey, value, Duration.ofSeconds(ttlSeconds));
        } catch (Exception e) {
            log.warn("Error writing histogram cache", e);
        }
    }

    /**
     * Generate domain-meaningful cache key for single metric query.
     * All key generation logic is centralized here in infrastructure.
     */
    private String generateCacheKey(MetricQuery query) {
        StringBuilder key = new StringBuilder(CACHE_PREFIX);
        key.append(query.getProjectId()).append(":");
        key.append(query.getMetricName()).append(":");
        key.append(query.getAggregationType().getValue()).append(":");
        key.append(query.getTimeRange().getStartTime().toEpochMilli()).append(":");
        key.append(query.getTimeRange().getEndTime().toEpochMilli()).append(":");
        key.append(query.getRollupPeriod().toPeriodString());
        
        // Include serviceId if present
        if (query.getServiceId().isPresent()) {
            key.append(":service:").append(query.getServiceId().get());
        }
        
        // Include tags if present (for future use)
        if (query.getTags().isPresent() && !query.getTags().get().isEmpty()) {
            query.getTags().get().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> key.append(":tag:").append(entry.getKey()).append("=").append(entry.getValue()));
        }
        
        return key.toString();
    }

    /**
     * Generate cache key for multi-metric query.
     */
    private String generateMultiCacheKey(MultiMetricQuery query) {
        StringBuilder key = new StringBuilder(MULTI_CACHE_PREFIX);
        key.append(query.getProjectId()).append(":");
        
        // Include all queries in deterministic order
        query.getQueries().stream()
                .sorted((a, b) -> {
                    int cmp = a.getMetricName().compareTo(b.getMetricName());
                    if (cmp != 0) return cmp;
                    return a.getAggregationType().getValue().compareTo(b.getAggregationType().getValue());
                })
                .forEach(q -> {
                    key.append(q.getMetricName()).append(":")
                       .append(q.getAggregationType().getValue()).append(":");
                    if (q.getServiceId() != null) {
                        key.append("service:").append(q.getServiceId()).append(":");
                    }
                });
        
        key.append(query.getTimeRange().getStartTime().toEpochMilli()).append(":")
           .append(query.getTimeRange().getEndTime().toEpochMilli()).append(":")
           .append(query.getRollupPeriod().toPeriodString());
        
        if (query.isAlignTimestamps()) {
            key.append(":aligned");
        }
        
        return key.toString();
    }

    /**
     * Generate cache key for histogram query.
     */
    private String generateHistogramCacheKey(HistogramQuery query) {
        StringBuilder key = new StringBuilder(HISTOGRAM_CACHE_PREFIX);
        key.append(query.getProjectId()).append(":");
        key.append(query.getMetricName()).append(":");
        key.append(query.getTimeRange().getStartTime().toEpochMilli()).append(":");
        key.append(query.getTimeRange().getEndTime().toEpochMilli()).append(":");
        key.append(query.getBins());
        
        if (query.getServiceId().isPresent()) {
            key.append(":service:").append(query.getServiceId().get());
        }
        
        return key.toString();
    }

    /**
     * Calculate cache TTL based on rollup period.
     * Shorter TTL for finer granularity to ensure freshness.
     */
    private int calculateCacheTtl(RollupPeriod rollupPeriod) {
        long minutes = rollupPeriod.getDuration().toMinutes();
        if (minutes <= 1) {
            return 60; // 1 minute TTL for 1m rollup
        } else if (minutes <= 5) {
            return 300; // 5 minutes TTL for 5m rollup
        } else {
            return 600; // 10 minutes TTL for 1h+ rollup
        }
    }
}
