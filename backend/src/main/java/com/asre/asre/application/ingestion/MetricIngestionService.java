package com.asre.asre.application.ingestion;

import com.asre.asre.application.service.ServiceDiscoveryService;
import com.asre.asre.domain.ingestion.Metric;
import com.asre.asre.domain.ingestion.MetricRepository;
import com.asre.asre.domain.ingestion.MetricsCollectorPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricIngestionService {

    private final MetricRepository metricRepository;
    private final MetricsCollectorPort metricsCollector;
    private final ServiceDiscoveryService serviceDiscoveryService;

    @Transactional
    public void ingestBatch(List<Metric> metricsList) {
        long startTime = System.currentTimeMillis();
        try {
            // Filter out invalid metrics
            List<Metric> validMetrics = metricsList.stream()
                    .filter(Metric::isValid)
                    .collect(Collectors.toList());

            if (validMetrics.isEmpty()) {
                log.warn("No valid metrics in batch");
                metricsCollector.recordMetricsIngestionError();
                return;
            }

            if (validMetrics.size() < metricsList.size()) {
                log.warn("Filtered out {} invalid metrics from batch of {}",
                        metricsList.size() - validMetrics.size(), metricsList.size());
            }

            // Auto-discover services from metrics
            validMetrics.stream()
                    .collect(Collectors.groupingBy(Metric::getServiceId))
                    .forEach((serviceId, metrics) -> {
                        if (serviceId != null && !metrics.isEmpty()) {
                            // Use serviceId as name if not found (temporary - can be improved later)
                            String serviceName = "service-" + serviceId.toString().substring(0, 8);
                            try {
                                serviceDiscoveryService.discoverService(
                                        metrics.get(0).getProjectId(),
                                        serviceId,
                                        serviceName
                                );
                            } catch (Exception e) {
                                log.warn("Failed to discover service {}: {}", serviceId, e.getMessage());
                            }
                        }
                    });

            metricRepository.saveBatch(validMetrics);
            metricsCollector.recordMetricsIngested(validMetrics.size());
            log.debug("Ingested {} metrics", validMetrics.size());
        } catch (Exception e) {
            metricsCollector.recordMetricsIngestionError();
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            metricsCollector.recordMetricsIngestionDuration(duration);
        }
    }
}
