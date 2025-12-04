package com.asre.asre.api.service;

import com.asre.asre.api.service.dto.ServiceDetailResponse;
import com.asre.asre.api.service.dto.ServiceOverviewResponse;
import com.asre.asre.api.service.dto.ServiceResponse;
import com.asre.asre.application.service.ServiceService;
import com.asre.asre.domain.alerts.Incident;
import com.asre.asre.domain.alerts.IncidentRepository;
import com.asre.asre.domain.project.Project;
import com.asre.asre.domain.project.ProjectRepository;
import com.asre.asre.domain.service.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
@Slf4j
public class ServicesController {

    private final ServiceService serviceService;
    private final ServicesDtoMapper mapper;
    private final IncidentRepository incidentRepository;
    private final ProjectRepository projectRepository;

    /**
     * Extract userId from JWT authentication.
     */
    private UUID getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("User not authenticated");
        }
        return UUID.fromString(authentication.getName());
    }

    /**
     * Validate that project belongs to user.
     */
    private void validateProjectOwnership(UUID projectId, UUID userId) {
        Project project = projectRepository.findByIdAndNotDeleted(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        project.ensureOwnedBy(userId);
    }

    @GetMapping
    public ResponseEntity<?> listServices(
            @RequestParam UUID projectId,
            Authentication authentication) {
        try {
            UUID userId = getCurrentUserId(authentication);
            validateProjectOwnership(projectId, userId);
            
            List<Service> services = serviceService.listServices(projectId);
            
            // Get recent incidents for each service
            List<ServiceResponse> responses = services.stream()
                    .map(service -> {
                        List<Incident> incidents = incidentRepository.findByServiceId(service.getId());
                        return mapper.toResponse(service, incidents);
                    })
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error listing services", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to list services"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getService(
            @PathVariable UUID id,
            @RequestParam UUID projectId,
            Authentication authentication) {
        try {
            UUID userId = getCurrentUserId(authentication);
            validateProjectOwnership(projectId, userId);
            
            Service service = serviceService.getService(id, projectId);
            List<Incident> incidents = incidentRepository.findByServiceId(id);
            int activeIncidents = (int) incidents.stream()
                    .filter(inc -> inc.getStatus().name().equals("OPEN") || 
                                  inc.getStatus().name().equals("ACKNOWLEDGED"))
                    .count();
            
            ServiceDetailResponse response = mapper.toDetailResponse(service, activeIncidents);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting service", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get service"));
        }
    }

    @GetMapping("/{id}/overview")
    public ResponseEntity<?> getServiceOverview(
            @PathVariable UUID id,
            @RequestParam UUID projectId,
            Authentication authentication) {
        try {
            UUID userId = getCurrentUserId(authentication);
            validateProjectOwnership(projectId, userId);
            
            ServiceService.ServiceOverview overview = serviceService.getServiceOverview(id, projectId);
            ServiceOverviewResponse response = mapper.toOverviewResponse(overview);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting service overview", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get service overview"));
        }
    }

    @GetMapping("/{id}/metrics")
    public ResponseEntity<?> getServiceMetrics(
            @PathVariable UUID id,
            @RequestParam UUID projectId,
            Authentication authentication) {
        try {
            UUID userId = getCurrentUserId(authentication);
            validateProjectOwnership(projectId, userId);
            
            // Validate service belongs to project
            serviceService.getService(id, projectId);
            
            // TODO: Implement actual metric name discovery
            // For MVP, return empty list
            List<String> metricNames = serviceService.getServiceMetrics(id, projectId);
            return ResponseEntity.ok(Map.of("metrics", metricNames));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting service metrics", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get service metrics"));
        }
    }
}


