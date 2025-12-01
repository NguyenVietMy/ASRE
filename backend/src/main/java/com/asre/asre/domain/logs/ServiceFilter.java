package com.asre.asre.domain.logs;

import lombok.Value;

import java.util.Optional;
import java.util.UUID;

/**
 * Value object representing a service filter for log queries.
 * Can be optional for global searches or required for service-specific views.
 */
@Value
public class ServiceFilter {
    Optional<UUID> serviceId;

    public ServiceFilter(UUID serviceId) {
        this.serviceId = Optional.ofNullable(serviceId);
    }

    public ServiceFilter() {
        this.serviceId = Optional.empty();
    }

    /**
     * Create a filter for a specific service.
     */
    public static ServiceFilter forService(UUID serviceId) {
        return new ServiceFilter(serviceId);
    }

    /**
     * Create an empty filter (no service restriction).
     */
    public static ServiceFilter any() {
        return new ServiceFilter();
    }

    /**
     * Check if this filter requires a specific service.
     */
    public boolean requiresService() {
        return serviceId.isPresent();
    }
}


