package com.asre.asre.application.service;

import com.asre.asre.domain.service.Service;
import com.asre.asre.domain.service.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Application service for service auto-discovery.
 * Called during ingestion to discover and register services.
 */
@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Slf4j
public class ServiceDiscoveryService {

    private final ServiceRepository serviceRepository;

    /**
     * Discover and register a service if it doesn't exist.
     * This is called during metric/log ingestion.
     * 
     * @param projectId The project ID
     * @param serviceId The service ID from the telemetry (may be null for new services)
     * @param serviceName The service name from the telemetry
     * @return The Service aggregate (existing or newly created)
     */
    @Transactional
    public Service discoverService(UUID projectId, UUID serviceId, String serviceName) {
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }
        if (serviceName == null || serviceName.isBlank()) {
            throw new IllegalArgumentException("Service name cannot be null or blank");
        }

        // If serviceId is provided, try to find existing service
        if (serviceId != null) {
            return serviceRepository.findById(serviceId)
                    .map(service -> {
                        // Validate it belongs to the project
                        service.ensureBelongsToProject(projectId);
                        // Update last seen
                        service.updateLastSeen();
                        return serviceRepository.save(service);
                    })
                    .orElseGet(() -> {
                        // Service ID provided but not found - create new with that ID
                        log.info("Service ID {} provided but not found, creating new service with name {}", 
                                serviceId, serviceName);
                        Service newService = Service.create(projectId, serviceName);
                        // Set the provided ID
                        newService.setId(serviceId);
                        newService.setLastSeenAt(Instant.now());
                        return serviceRepository.save(newService);
                    });
        }

        // No serviceId provided - try to find by name
        return serviceRepository.findByProjectIdAndName(projectId, serviceName)
                .map(service -> {
                    service.updateLastSeen();
                    return serviceRepository.save(service);
                })
                .orElseGet(() -> createNewService(projectId, serviceName));
    }

    private Service createNewService(UUID projectId, String serviceName) {
        Service service = Service.create(projectId, serviceName);
        Service saved = serviceRepository.save(service);
        log.info("Auto-discovered new service: {} for project {}", serviceName, projectId);
        return saved;
    }
}

