package com.asre.asre.infra.opensearch;

import com.asre.asre.domain.logs.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.common.unit.TimeValue;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.aggregations.AggregationBuilders;
import org.opensearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.opensearch.search.aggregations.bucket.histogram.Histogram;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * OpenSearch implementation of LogVolumeRepository.
 * Uses date_histogram aggregations for volume queries.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class OpenSearchLogVolumeRepository implements LogVolumeRepository {

    private final RestHighLevelClient openSearchClient;

    @Value("${opensearch.index.name:opensearch-logs}")
    private String indexName;

    @Override
    public LogVolumeResult executeVolumeQuery(LogVolumeQuery query) {
        try {
            SearchRequest searchRequest = buildVolumeSearchRequest(query);
            SearchResponse response = openSearchClient.search(searchRequest, RequestOptions.DEFAULT);

            Histogram histogram = response.getAggregations().get("volume_over_time");
            List<LogVolumePoint> dataPoints = new ArrayList<>();
            boolean isSampled = false;

            for (Histogram.Bucket bucket : histogram.getBuckets()) {
                long epochMillis = ((Number) bucket.getKey()).longValue();
                Instant timestamp = Instant.ofEpochMilli(epochMillis);
                long count = bucket.getDocCount();
                
                // Check if sampling was applied (could be stored in metadata)
                // For now, we'll infer from the query or store it separately
                LogVolumePoint point = new LogVolumePoint(timestamp, count);
                dataPoints.add(point);
            }

            return new LogVolumeResult(dataPoints, isSampled);
        } catch (Exception e) {
            log.error("Error executing volume query", e);
            throw new RuntimeException("Failed to execute volume query", e);
        }
    }

    @Override
    public ErrorSpikeResult executeErrorSpikeQuery(ErrorSpikeQuery query) {
        try {
            // Get raw data buckets (ERROR level only)
            // Infrastructure only fetches raw counts; domain interprets spikes
            LogVolumeQuery volumeQuery = new LogVolumeQuery(
                    query.getProjectId(),
                    query.getTimeRange(),
                    query.getRollupPeriod(),
                    query.getServiceFilter().orElse(null),
                    LogLevelFilter.errorsOnly() // ERROR and FATAL only
            );

            LogVolumeResult volumeResult = executeVolumeQuery(volumeQuery);
            List<LogVolumePoint> allDataPoints = volumeResult.getDataPoints();

            // Return raw data only - spike detection happens in application/domain layer
            // This method should not exist here, but keeping for interface compliance
            // Application service will call executeVolumeQuery directly and use ErrorSpikeDetector
            return new ErrorSpikeResult(List.of(), allDataPoints);
        } catch (Exception e) {
            log.error("Error executing error spike query", e);
            throw new RuntimeException("Failed to execute error spike query", e);
        }
    }

    private SearchRequest buildVolumeSearchRequest(LogVolumeQuery query) {
        SearchRequest searchRequest = new SearchRequest(indexName);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // Build query
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("project_id", query.getProjectId().toString()));

        // Time range
        boolQuery.must(QueryBuilders.rangeQuery("timestamp")
                .gte(query.getTimeRange().getStartTime().toString())
                .lte(query.getTimeRange().getEndTime().toString()));

        // Service filter
        query.getServiceFilter().ifPresent(filter -> {
            if (filter.requiresService()) {
                boolQuery.must(QueryBuilders.termQuery("service_id", 
                        filter.getServiceId().get().toString()));
            }
        });

        // Level filter
        query.getLevelFilter().ifPresent(filter -> {
            List<String> levels = filter.getLevels().stream()
                    .map(LogLevel::name)
                    .collect(Collectors.toList());
            boolQuery.must(QueryBuilders.termsQuery("level", levels));
        });

        sourceBuilder.query(boolQuery);
        sourceBuilder.size(0); // We only want aggregations

        // Date histogram aggregation
        String interval = formatInterval(query.getRollupPeriod());
        sourceBuilder.aggregation(
                AggregationBuilders.dateHistogram("volume_over_time")
                        .field("timestamp")
                        .fixedInterval(new DateHistogramInterval(interval))
                        .minDocCount(0) // Include empty buckets
        );

        sourceBuilder.timeout(TimeValue.timeValueSeconds(30));
        searchRequest.source(sourceBuilder);

        return searchRequest;
    }

    private String formatInterval(com.asre.asre.domain.metrics.RollupPeriod rollupPeriod) {
        long minutes = rollupPeriod.getDuration().toMinutes();
        if (minutes < 60) {
            return minutes + "m";
        }
        long hours = rollupPeriod.getDuration().toHours();
        if (hours < 24) {
            return hours + "h";
        }
        long days = rollupPeriod.getDuration().toDays();
        return days + "d";
    }

}

