package com.asre.asre.application.ingestion;

import com.asre.asre.application.logs.LogSamplingService;
import com.asre.asre.application.service.ServiceDiscoveryService;
import com.asre.asre.domain.ingestion.LogEntry;
import com.asre.asre.domain.ingestion.LogIndexer;
import com.asre.asre.domain.ingestion.LogMetadata;
import com.asre.asre.domain.ingestion.LogMetadataRepository;
import com.asre.asre.domain.ingestion.MetricsCollector;
import com.asre.asre.domain.logs.LogId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogIngestionService {

    private final LogMetadataRepository logMetadataRepository;
    private final LogIndexer logIndexer;
    private final MetricsCollector metricsCollector;
    private final ServiceDiscoveryService serviceDiscoveryService;
    private final LogSamplingService samplingService;

    @Transactional
    public void ingestBatch(List<LogEntry> logEntries) {
        long startTime = System.currentTimeMillis();
        try {
            // Filter out invalid logs
            List<LogEntry> validLogs = logEntries.stream()
                    .filter(LogEntry::isValid)
                    .collect(Collectors.toList());

            if (validLogs.isEmpty()) {
                log.warn("No valid logs in batch");
                return;
            }

            if (validLogs.size() < logEntries.size()) {
                log.warn("Filtered out {} invalid logs from batch of {}",
                        logEntries.size() - validLogs.size(), logEntries.size());
            }

            if (validLogs.isEmpty()) {
                return;
            }

            UUID projectId = validLogs.get(0).getProjectId();

            // Auto-discover services from logs
            validLogs.stream()
                    .collect(Collectors.groupingBy(LogEntry::getServiceId))
                    .forEach((serviceId, logs) -> {
                        if (serviceId != null && !logs.isEmpty()) {
                            // Use serviceId as name if not found (temporary - can be improved later)
                            String serviceName = "service-" + serviceId.toString().substring(0, 8);
                            try {
                                serviceDiscoveryService.discoverService(
                                        logs.get(0).getProjectId(),
                                        serviceId,
                                        serviceName
                                );
                            } catch (Exception e) {
                                log.warn("Failed to discover service {}: {}", serviceId, e.getMessage());
                            }
                        }
                    });

            // Apply sampling policy (domain rule enforcement)
            List<LogEntry> sampledLogs = samplingService.applySampling(validLogs, projectId);
            
            if (sampledLogs.isEmpty()) {
                log.debug("All logs filtered out by sampling policy");
                return;
            }

            // Write to OpenSearch and create metadata
            List<LogMetadata> metadataList = sampledLogs.stream()
                    .map(log -> {
                        // Set LogId if not already set
                        LogId logId = log.getLogId() != null 
                                ? log.getLogId() 
                                : log.getOrCreateLogId();
                        log.setLogId(logId);
                        
                        // Write to search engine (OpenSearch)
                        logIndexer.indexLog(log, logId.getValue());
                        
                        // Create metadata
                        return new LogMetadata(
                                log.getProjectId(),
                                log.getServiceId(),
                                logId.getValue(),
                                log.getTimestamp(),
                                Instant.now(), // ingested_at
                                log.getLevel().name()); // Convert enum to string for storage
                    })
                    .collect(Collectors.toList());

            // Save metadata to PostgreSQL
            logMetadataRepository.saveBatch(metadataList);
            metricsCollector.recordLogsIngested(sampledLogs.size());
            log.debug("Ingested {} logs ({} after sampling)", sampledLogs.size(), validLogs.size());
        } catch (Exception e) {
            metricsCollector.recordLogsIngestionError();
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            metricsCollector.recordLogsIngestionDuration(duration);
        }
    }
}
