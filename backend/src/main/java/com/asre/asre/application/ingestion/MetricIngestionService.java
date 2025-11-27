package com.asre.asre.application.ingestion;

import com.asre.asre.domain.ingestion.Metric;
import com.asre.asre.domain.ingestion.MetricRepository;
import com.asre.asre.domain.ingestion.MetricsCollector;
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
    private final MetricsCollector metricsCollector;

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
