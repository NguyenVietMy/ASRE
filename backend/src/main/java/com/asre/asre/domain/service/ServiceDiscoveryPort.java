package com.asre.asre.domain.service;

import java.util.UUID;

/**
 * Domain port for service discovery.
 * Allows infrastructure to discover services without depending on application layer.
 */
public interface ServiceDiscoveryPort {
    /**
     * Discover and register a service if it doesn't exist.
     * This is called during metric/log ingestion.
     * 
     * @param projectId The project ID
     * @param serviceId The service ID from the telemetry (may be null for new services)
     * @param serviceName The service name from the telemetry
     * @return The Service aggregate (existing or newly created)
     */
    Service discoverService(UUID projectId, UUID serviceId, String serviceName);
}

