package com.asre.asre.api.metrics;

import com.asre.asre.api.metrics.dto.*;
import com.asre.asre.domain.metrics.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper between API DTOs and domain objects for metrics.
 */
@Component
public class MetricsDtoMapper {

    public MetricQuery toDomainQuery(MetricQueryRequest request, UUID projectId) {
        return new MetricQuery(
                projectId,
                request.getMetric(),
                AggregationType.fromString(request.getStat()),
                new TimeRange(
                        Instant.parse(request.getStartTime()),
                        Instant.parse(request.getEndTime())
                ),
                new RollupPeriod(request.getRollup()),
                request.getServiceId(),
                null // Tags not supported in request yet
        );
    }

    public MultiMetricQuery toDomainMultiQuery(MultiMetricQueryRequest request, UUID projectId) {
        List<MultiMetricQuery.SingleMetricQuery> queries = request.getQueries().stream()
                .map(q -> new MultiMetricQuery.SingleMetricQuery(
                        q.getMetric(),
                        AggregationType.fromString(q.getStat()),
                        q.getServiceId()
                ))
                .collect(Collectors.toList());

        return new MultiMetricQuery(
                projectId,
                queries,
                new TimeRange(
                        Instant.parse(request.getStartTime()),
                        Instant.parse(request.getEndTime())
                ),
                new RollupPeriod(request.getRollup()),
                request.isAlign()
        );
    }

    public HistogramQuery toDomainHistogramQuery(HistogramQueryRequest request, UUID projectId) {
        return new HistogramQuery(
                projectId,
                request.getMetric(),
                new TimeRange(
                        Instant.parse(request.getStartTime()),
                        Instant.parse(request.getEndTime())
                ),
                request.getBins(),
                request.getServiceId()
        );
    }

    public AnomalyDetectionQuery toDomainAnomalyQuery(AnomalyDetectionRequest request, UUID projectId) {
        return new AnomalyDetectionQuery(
                projectId,
                request.getMetric(),
                new TimeRange(
                        Instant.parse(request.getStartTime()),
                        Instant.parse(request.getEndTime())
                ),
                AnomalyDetectionQuery.AnomalyDetectionMethod.fromString(request.getMethod()),
                request.getServiceId()
        );
    }

    public MetricQueryResponse toResponse(MetricQueryResult result) {
        List<MetricQueryResponse.DataPoint> dataPoints = result.getDataPoints().stream()
                .map(p -> new MetricQueryResponse.DataPoint(
                        p.getTimestamp().toString(),
                        p.getValue()
                ))
                .collect(Collectors.toList());

        return new MetricQueryResponse(
                result.getMetricName(),
                result.getAggregationType().getValue(),
                dataPoints
        );
    }

    public MultiMetricQueryResponse toMultiResponse(MultiMetricQueryResult result) {
        List<MetricQueryResponse> responses = result.getResults().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return new MultiMetricQueryResponse(responses);
    }

    public HistogramResponse toHistogramResponse(HistogramResult result) {
        List<HistogramResponse.Bin> bins = result.getBins().stream()
                .map(b -> new HistogramResponse.Bin(
                        b.getMinValue(),
                        b.getMaxValue(),
                        b.getCount()
                ))
                .collect(Collectors.toList());

        return new HistogramResponse(result.getMetricName(), bins);
    }

    public AnomalyDetectionResponse toAnomalyResponse(List<AnomalyDetectionResult> results) {
        List<AnomalyDetectionResponse.AnomalyResult> anomalies = results.stream()
                .filter(AnomalyDetectionResult::getIsAnomaly)
                .map(r -> {
                    Double value = r.getContext() != null && r.getContext().containsKey("value")
                            ? ((Number) r.getContext().get("value")).doubleValue()
                            : null;
                    return new AnomalyDetectionResponse.AnomalyResult(
                            r.getTimestamp().toString(),
                            r.getZScore(),
                            r.getIsAnomaly(),
                            value
                    );
                })
                .collect(Collectors.toList());

        return new AnomalyDetectionResponse(anomalies);
    }
}

