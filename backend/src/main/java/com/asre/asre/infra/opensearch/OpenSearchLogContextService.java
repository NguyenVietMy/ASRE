package com.asre.asre.infra.opensearch;

import com.asre.asre.domain.ingestion.LogEntry;
import com.asre.asre.domain.logs.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Infrastructure implementation of LogContextService.
 * Executes queries to get logs before and after a target log.
 * Domain policy validation is handled by application layer.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OpenSearchLogContextService implements LogContextServicePort {

    private final RestHighLevelClient openSearchClient;
    private final OpenSearchLogQueryRepository queryRepository;

    @Value("${opensearch.index.name:opensearch-logs}")
    private String indexName;

    @Override
    public LogContext getContext(LogId logId, UUID projectId, int beforeCount, int afterCount) {
        // Domain policy validation should be done in application layer
        // Infrastructure just executes queries

        // Find target log
        LogEntry targetLog = queryRepository.findById(logId, projectId);

        // Get logs before (earlier timestamp, or same timestamp but earlier ingested_at)
        List<LogEntry> beforeLogs = getLogsBefore(targetLog, projectId, beforeCount);

        // Get logs after (later timestamp, or same timestamp but later ingested_at)
        List<LogEntry> afterLogs = getLogsAfter(targetLog, projectId, afterCount);

        return new LogContextServicePort.LogContext(targetLog, beforeLogs, afterLogs);
    }

    private List<LogEntry> getLogsBefore(LogEntry targetLog, UUID projectId, int count) {
        if (count == 0) {
            return List.of();
        }

        try {
            SearchRequest searchRequest = new SearchRequest(indexName);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                    .must(QueryBuilders.termQuery("project_id", projectId.toString()))
                    .must(QueryBuilders.rangeQuery("timestamp")
                            .lte(targetLog.getTimestamp().toString()));

            // Exclude the target log itself if we're looking for strictly before
            boolQuery.mustNot(QueryBuilders.termQuery("_id", targetLog.getLogId().getValue()));

            sourceBuilder.query(boolQuery);
            sourceBuilder.sort("timestamp", SortOrder.DESC);
            sourceBuilder.sort("ingested_at", SortOrder.DESC);
            sourceBuilder.sort("_id", SortOrder.DESC);
            sourceBuilder.size(count);

            SearchResponse response = openSearchClient.search(searchRequest, RequestOptions.DEFAULT);
            return OpenSearchLogEntryMapper.parseLogEntries(response.getHits().getHits());
        } catch (Exception e) {
            log.error("Error getting logs before", e);
            return List.of();
        }
    }

    private List<LogEntry> getLogsAfter(LogEntry targetLog, UUID projectId, int count) {
        if (count == 0) {
            return List.of();
        }

        try {
            SearchRequest searchRequest = new SearchRequest(indexName);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                    .must(QueryBuilders.termQuery("project_id", projectId.toString()))
                    .must(QueryBuilders.rangeQuery("timestamp")
                            .gte(targetLog.getTimestamp().toString()));

            // Exclude the target log itself
            boolQuery.mustNot(QueryBuilders.termQuery("_id", targetLog.getLogId().getValue()));

            sourceBuilder.query(boolQuery);
            sourceBuilder.sort("timestamp", SortOrder.ASC);
            sourceBuilder.sort("ingested_at", SortOrder.ASC);
            sourceBuilder.sort("_id", SortOrder.ASC);
            sourceBuilder.size(count);

            SearchResponse response = openSearchClient.search(searchRequest, RequestOptions.DEFAULT);
            return OpenSearchLogEntryMapper.parseLogEntries(response.getHits().getHits());
        } catch (Exception e) {
            log.error("Error getting logs after", e);
            return List.of();
        }
    }
}


