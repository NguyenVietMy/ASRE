package com.asre.asre.api.metrics;

import com.asre.asre.api.metrics.dto.*;
import com.asre.asre.application.metrics.MetricsQueryService;
import com.asre.asre.domain.metrics.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
@Slf4j
public class MetricsController {

    private final MetricsQueryService metricsQueryService;
    private final MetricsDtoMapper mapper;

    @GetMapping("/query")
    public ResponseEntity<?> queryMetrics(
            @Valid MetricQueryRequest request,
            @RequestHeader("X-Project-ID") String projectIdHeader) {
        try {
            UUID projectId = UUID.fromString(projectIdHeader);
            MetricQuery query = mapper.toDomainQuery(request, projectId);
            MetricQueryResult result = metricsQueryService.queryMetrics(query);
            return ResponseEntity.ok(mapper.toResponse(result));
        } catch (InvalidMetricQueryException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error querying metrics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to query metrics"));
        }
    }

    @PostMapping("/query-multiple")
    public ResponseEntity<?> queryMultipleMetrics(
            @Valid @RequestBody MultiMetricQueryRequest request,
            @RequestHeader("X-Project-ID") String projectIdHeader) {
        try {
            UUID projectId = UUID.fromString(projectIdHeader);
            MultiMetricQuery query = mapper.toDomainMultiQuery(request, projectId);
            MultiMetricQueryResult result = metricsQueryService.queryMultipleMetrics(query);
            return ResponseEntity.ok(mapper.toMultiResponse(result));
        } catch (InvalidMetricQueryException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error querying multiple metrics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to query metrics"));
        }
    }

    @GetMapping("/histogram")
    public ResponseEntity<?> queryHistogram(
            @Valid HistogramQueryRequest request,
            @RequestHeader("X-Project-ID") String projectIdHeader) {
        try {
            UUID projectId = UUID.fromString(projectIdHeader);
            HistogramQuery query = mapper.toDomainHistogramQuery(request, projectId);
            HistogramResult result = metricsQueryService.queryHistogram(query);
            return ResponseEntity.ok(mapper.toHistogramResponse(result));
        } catch (InvalidMetricQueryException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error querying histogram", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to query histogram"));
        }
    }

    @GetMapping("/anomalies")
    public ResponseEntity<?> detectAnomalies(
            @Valid AnomalyDetectionRequest request,
            @RequestHeader("X-Project-ID") String projectIdHeader) {
        try {
            UUID projectId = UUID.fromString(projectIdHeader);
            AnomalyDetectionQuery query = mapper.toDomainAnomalyQuery(request, projectId);
            var results = metricsQueryService.detectAnomalies(query);
            return ResponseEntity.ok(mapper.toAnomalyResponse(results));
        } catch (InvalidMetricQueryException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error detecting anomalies", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to detect anomalies"));
        }
    }
}

