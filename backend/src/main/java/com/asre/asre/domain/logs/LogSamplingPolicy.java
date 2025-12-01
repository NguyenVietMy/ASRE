package com.asre.asre.domain.logs;

import lombok.Value;

import java.util.Map;
import java.util.UUID;

/**
 * Domain rule representing log sampling policy.
 * Configurable per project, service, and level.
 * Enforcement happens in infrastructure, but policy lives in domain.
 */
@Value
public class LogSamplingPolicy {
    UUID projectId;
    Map<LogLevel, Double> defaultRates; // Default rates per level
    Map<UUID, Map<LogLevel, Double>> serviceOverrides; // Per-service overrides
    double minErrorRate; // Minimum rate for ERROR in production (domain-enforced bound)

    public LogSamplingPolicy(
            UUID projectId,
            Map<LogLevel, Double> defaultRates,
            Map<UUID, Map<LogLevel, Double>> serviceOverrides,
            double minErrorRate) {
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }
        if (defaultRates == null || defaultRates.isEmpty()) {
            throw new IllegalArgumentException("Default rates cannot be null or empty");
        }
        if (minErrorRate < 0.0 || minErrorRate > 1.0) {
            throw new IllegalArgumentException("Minimum error rate must be between 0 and 1");
        }
        
        // Validate all rates are between 0 and 1
        defaultRates.values().forEach(rate -> {
            if (rate < 0.0 || rate > 1.0) {
                throw new IllegalArgumentException("Sampling rate must be between 0 and 1");
            }
        });
        
        // Validate ERROR rate meets minimum
        Double errorRate = defaultRates.get(LogLevel.ERROR);
        if (errorRate != null && errorRate < minErrorRate) {
            throw new IllegalArgumentException(
                    String.format("ERROR sampling rate (%.2f) is below minimum (%.2f)", errorRate, minErrorRate));
        }
        
        this.projectId = projectId;
        this.defaultRates = Map.copyOf(defaultRates);
        this.serviceOverrides = serviceOverrides != null ? Map.copyOf(serviceOverrides) : Map.of();
        this.minErrorRate = minErrorRate;
    }

    /**
     * Get the sampling rate for a given service and level.
     * Checks service overrides first, then falls back to defaults.
     */
    public double getSamplingRate(UUID serviceId, LogLevel level) {
        // Check service override
        Map<LogLevel, Double> serviceRates = serviceOverrides.get(serviceId);
        if (serviceRates != null && serviceRates.containsKey(level)) {
            return serviceRates.get(level);
        }
        
        // Fall back to default
        return defaultRates.getOrDefault(level, 1.0); // Default to 100% if not specified
    }

    /**
     * Create a default policy with standard rates.
     * INFO=5%, DEBUG=1%, ERROR=100% (unsampled), others=100%
     */
    public static LogSamplingPolicy defaultPolicy(UUID projectId) {
        Map<LogLevel, Double> defaults = Map.of(
                LogLevel.INFO, 0.05,
                LogLevel.DEBUG, 0.01,
                LogLevel.ERROR, 1.0,
                LogLevel.FATAL, 1.0,
                LogLevel.WARN, 1.0,
                LogLevel.TRACE, 1.0
        );
        return new LogSamplingPolicy(projectId, defaults, Map.of(), 1.0); // ERROR must be 100% in default
    }
}

