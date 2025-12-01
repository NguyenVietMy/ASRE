package com.asre.asre.infra.redis;

import com.asre.asre.application.logs.LogCache;
import com.asre.asre.domain.logs.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis implementation of LogCache.
 * Centralizes all cache key generation and TTL calculation in infrastructure layer.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LogCacheImpl implements LogCache {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String VOLUME_CACHE_PREFIX = "log:volume:";
    private static final String ERROR_SPIKE_CACHE_PREFIX = "log:error-spike:";

    @Override
    public Optional<LogVolumeResult> getVolume(LogVolumeQuery query) {
        try {
            String cacheKey = generateVolumeCacheKey(query);
            String value = redisTemplate.opsForValue().get(cacheKey);
            if (value == null) {
                return Optional.empty();
            }
            LogVolumeResult result = objectMapper.readValue(value, LogVolumeResult.class);
            return Optional.of(result);
        } catch (Exception e) {
            log.warn("Error reading volume cache", e);
            return Optional.empty();
        }
    }

    @Override
    public void putVolume(LogVolumeQuery query, LogVolumeResult result) {
        try {
            String cacheKey = generateVolumeCacheKey(query);
            int ttlSeconds = calculateCacheTtl(query.getRollupPeriod());
            String value = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(cacheKey, value, Duration.ofSeconds(ttlSeconds));
        } catch (Exception e) {
            log.warn("Error writing volume cache", e);
        }
    }

    @Override
    public Optional<ErrorSpikeResult> getErrorSpikes(ErrorSpikeQuery query) {
        try {
            String cacheKey = generateErrorSpikeCacheKey(query);
            String value = redisTemplate.opsForValue().get(cacheKey);
            if (value == null) {
                return Optional.empty();
            }
            ErrorSpikeResult result = objectMapper.readValue(value, ErrorSpikeResult.class);
            return Optional.of(result);
        } catch (Exception e) {
            log.warn("Error reading error spike cache", e);
            return Optional.empty();
        }
    }

    @Override
    public void putErrorSpikes(ErrorSpikeQuery query, ErrorSpikeResult result) {
        try {
            String cacheKey = generateErrorSpikeCacheKey(query);
            int ttlSeconds = calculateCacheTtl(query.getRollupPeriod());
            String value = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(cacheKey, value, Duration.ofSeconds(ttlSeconds));
        } catch (Exception e) {
            log.warn("Error writing error spike cache", e);
        }
    }

    /**
     * Generate domain-meaningful cache key for volume query.
     */
    private String generateVolumeCacheKey(LogVolumeQuery query) {
        StringBuilder key = new StringBuilder(VOLUME_CACHE_PREFIX);
        key.append(query.getProjectId()).append(":");
        key.append(query.getTimeRange().getStartTime().toEpochMilli()).append(":");
        key.append(query.getTimeRange().getEndTime().toEpochMilli()).append(":");
        key.append(query.getRollupPeriod().toPeriodString());
        
        query.getServiceFilter().ifPresent(filter -> {
            if (filter.requiresService()) {
                key.append(":service:").append(filter.getServiceId().get());
            }
        });
        
        query.getLevelFilter().ifPresent(filter -> {
            key.append(":levels:");
            filter.getLevels().forEach(level -> key.append(level.name()).append(","));
        });
        
        return key.toString();
    }

    /**
     * Generate cache key for error spike query.
     */
    private String generateErrorSpikeCacheKey(ErrorSpikeQuery query) {
        StringBuilder key = new StringBuilder(ERROR_SPIKE_CACHE_PREFIX);
        key.append(query.getProjectId()).append(":");
        key.append(query.getTimeRange().getStartTime().toEpochMilli()).append(":");
        key.append(query.getTimeRange().getEndTime().toEpochMilli()).append(":");
        key.append(query.getRollupPeriod().toPeriodString());
        
        query.getServiceFilter().ifPresent(filter -> {
            if (filter.requiresService()) {
                key.append(":service:").append(filter.getServiceId().get());
            }
        });
        
        query.getSpikeThreshold().ifPresent(threshold -> {
            key.append(":threshold:").append(threshold);
        });
        
        return key.toString();
    }

    /**
     * Calculate cache TTL based on rollup period.
     */
    private int calculateCacheTtl(com.asre.asre.domain.metrics.RollupPeriod rollupPeriod) {
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


