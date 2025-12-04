package com.asre.asre.application.service;

import com.asre.asre.domain.alerts.IncidentRepository;
import com.asre.asre.domain.metrics.MetricQueryRepository;
import com.asre.asre.domain.metrics.MetricQuery;
import com.asre.asre.domain.metrics.MetricQueryResult;
import com.asre.asre.domain.metrics.TimeRange;
import com.asre.asre.domain.metrics.RollupPeriod;
import com.asre.asre.domain.service.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Application service for service management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceService {

    private final ServiceRepository serviceRepository;
    private final IncidentRepository incidentRepository;
    private final MetricQueryRepository metricQueryRepository;

    /**
     * Lists all services for a project.
     */
    public List<com.asre.asre.domain.service.Service> listServices(UUID projectId) {
        return serviceRepository.findByProjectId(projectId);
    }

    /**
     * Gets a service by ID, ensuring it belongs to the project.
     */
    public com.asre.asre.domain.service.Service getService(UUID serviceId, UUID projectId) {
        com.asre.asre.domain.service.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Service not found: " + serviceId));
        service.ensureBelongsToProject(projectId);
        return service;
    }

    /**
     * Gets list of metric names seen for a service.
     */
    public List<String> getServiceMetrics(UUID serviceId, UUID projectId) {
        // Validate service belongs to project
        getService(serviceId, projectId);

        // TODO: Implement actual metric name discovery
        // This would require a new repository method:
        // MetricRepository.findDistinctMetricNames(projectId, serviceId)
        // For MVP, return empty list
        return List.of();
    }

    /**
     * Gets service overview with aggregated metrics.
     */
    public ServiceOverview getServiceOverview(UUID serviceId, UUID projectId) {
        com.asre.asre.domain.service.Service service = getService(serviceId, projectId);

        // Get recent time range (last hour)
        Instant endTime = Instant.now();
        Instant startTime = endTime.minus(Duration.ofHours(1));
        TimeRange timeRange = new TimeRange(startTime, endTime);
        RollupPeriod rollupPeriod = new RollupPeriod(Duration.ofMinutes(1));

        // Query error rate
        Double errorRate = queryErrorRate(projectId, serviceId, timeRange, rollupPeriod);

        // Query p95 latency
        Double p95Latency = queryP95Latency(projectId, serviceId, timeRange, rollupPeriod);

        // Query request throughput
        Double throughput = queryThroughput(projectId, serviceId, timeRange, rollupPeriod);

        // Count open incidents
        long openIncidents = incidentRepository.findByServiceId(serviceId).stream()
                .filter(incident -> incident.getStatus().name().equals("OPEN") ||
                        incident.getStatus().name().equals("ACKNOWLEDGED"))
                .count();

        return new ServiceOverview(
                service.getId(),
                service.getName(),
                errorRate,
                p95Latency,
                throughput,
                (int) openIncidents);
    }

    private Double queryErrorRate(UUID projectId, UUID serviceId, TimeRange timeRange, RollupPeriod rollupPeriod) {
        try {
            MetricQuery query = new MetricQuery(
                    projectId,
                    "service_error_rate",
                    com.asre.asre.domain.metrics.AggregationType.AVG,
                    timeRange,
                    rollupPeriod,
                    serviceId,
                    null);
            MetricQueryResult result = metricQueryRepository.executeQuery(query);
            if (result.getDataPoints().isEmpty()) {
                return null;
            }
            // Return the most recent value
            return result.getDataPoints().get(result.getDataPoints().size() - 1).getValue();
        } catch (Exception e) {
            log.debug("Error querying error rate for service {}", serviceId, e);
            return null;
        }
    }

    private Double queryP95Latency(UUID projectId, UUID serviceId, TimeRange timeRange, RollupPeriod rollupPeriod) {
        try {
            MetricQuery query = new MetricQuery(
                    projectId,
                    "http_server_duration",
                    com.asre.asre.domain.metrics.AggregationType.P95,
                    timeRange,
                    rollupPeriod,
                    serviceId,
                    null);
            MetricQueryResult result = metricQueryRepository.executeQuery(query);
            if (result.getDataPoints().isEmpty()) {
                return null;
            }
            return result.getDataPoints().get(result.getDataPoints().size() - 1).getValue();
        } catch (Exception e) {
            log.debug("Error querying p95 latency for service {}", serviceId, e);
            return null;
        }
    }

    private Double queryThroughput(UUID projectId, UUID serviceId, TimeRange timeRange, RollupPeriod rollupPeriod) {
        try {
            MetricQuery query = new MetricQuery(
                    projectId,
                    "http_requests_total",
                    com.asre.asre.domain.metrics.AggregationType.SUM,
                    timeRange,
                    rollupPeriod,
                    serviceId,
                    null);
            MetricQueryResult result = metricQueryRepository.executeQuery(query);
            if (result.getDataPoints().isEmpty()) {
                return null;
            }
            // Sum all data points for total requests
            return result.getDataPoints().stream()
                    .mapToDouble(point -> point.getValue())
                    .sum();
        } catch (Exception e) {
            log.debug("Error querying throughput for service {}", serviceId, e);
            return null;
        }
    }

    /**
     * Service overview result.
     */
    public record ServiceOverview(
            UUID serviceId,
            String serviceName,
            Double errorRate,
            Double p95Latency,
            Double throughput,
            int openIncidents) {
    }
}
