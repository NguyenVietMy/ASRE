package com.asre.asre.api.service;

import com.asre.asre.api.service.dto.ServiceDetailResponse;
import com.asre.asre.api.service.dto.ServiceOverviewResponse;
import com.asre.asre.api.service.dto.ServiceResponse;
import com.asre.asre.application.service.ServiceService;
import com.asre.asre.domain.alerts.Incident;
import com.asre.asre.domain.service.Service;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
public class ServicesDtoMapper {

    public ServiceResponse toResponse(Service service, List<Incident> recentIncidents) {
        String status = determineStatus(service, recentIncidents);
        int incidentCount = (int) recentIncidents.stream()
                .filter(inc -> inc.getStatus().name().equals("OPEN") || 
                              inc.getStatus().name().equals("ACKNOWLEDGED"))
                .count();
        
        return new ServiceResponse(
                service.getId(),
                service.getName(),
                service.getCreatedAt(),
                service.getLastSeenAt(),
                status,
                incidentCount
        );
    }

    public ServiceDetailResponse toDetailResponse(Service service, int activeIncidents) {
        return new ServiceDetailResponse(
                service.getId(),
                service.getName(),
                service.getProjectId(),
                service.getCreatedAt(),
                service.getLastSeenAt(),
                activeIncidents
        );
    }

    public ServiceOverviewResponse toOverviewResponse(ServiceService.ServiceOverview overview) {
        return new ServiceOverviewResponse(
                overview.serviceId(),
                overview.serviceName(),
                overview.errorRate(),
                overview.p95Latency(),
                overview.throughput(),
                overview.openIncidents()
        );
    }

    private String determineStatus(Service service, List<Incident> recentIncidents) {
        Instant now = Instant.now();
        Instant lastSeen = service.getLastSeenAt();
        
        if (lastSeen == null) {
            return "Down";
        }
        
        Duration timeSinceLastSeen = Duration.between(lastSeen, now);
        
        // If last seen > 5 minutes ago, consider it Down
        if (timeSinceLastSeen.toMinutes() > 5) {
            return "Down";
        }
        
        // Check for critical incidents
        boolean hasCriticalIncidents = recentIncidents.stream()
                .anyMatch(inc -> inc.getSeverity().name().equals("CRITICAL") && 
                               (inc.getStatus().name().equals("OPEN") || 
                                inc.getStatus().name().equals("ACKNOWLEDGED")));
        
        if (hasCriticalIncidents) {
            return "Degraded";
        }
        
        return "Healthy";
    }
}

