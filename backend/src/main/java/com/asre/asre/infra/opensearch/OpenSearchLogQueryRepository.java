package com.asre.asre.infra.opensearch;

import com.asre.asre.domain.ingestion.LogEntry;
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
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * OpenSearch implementation of LogQueryRepository.
 * Maps domain queries to OpenSearch SearchRequest and results back to domain objects.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class OpenSearchLogQueryRepository implements LogQueryRepository {

    private final RestHighLevelClient openSearchClient;

    @Value("${opensearch.index.name:opensearch-logs}")
    private String indexName;

    @Override
    public LogQueryResult executeQuery(LogQuery query) {
        try {
            SearchRequest searchRequest = buildSearchRequest(query);
            SearchResponse response = openSearchClient.search(searchRequest, RequestOptions.DEFAULT);

            List<LogEntry> logs = parseLogEntries(response.getHits().getHits());
            LogPaginationToken nextToken = extractPaginationToken(response, logs);

            return new LogQueryResult(logs, nextToken);
        } catch (Exception e) {
            log.error("Error executing log query", e);
            throw new RuntimeException("Failed to execute log query", e);
        }
    }

    @Override
    public LogEntry findById(LogId logId, java.util.UUID projectId) {
        try {
            SearchRequest searchRequest = new SearchRequest(indexName);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                    .must(QueryBuilders.termQuery("_id", logId.getValue()))
                    .must(QueryBuilders.termQuery("project_id", projectId.toString()));
            
            sourceBuilder.query(boolQuery);
            sourceBuilder.size(1);
            searchRequest.source(sourceBuilder);

            SearchResponse response = openSearchClient.search(searchRequest, RequestOptions.DEFAULT);
            
            if (response.getHits().getTotalHits().value == 0) {
                throw new LogNotFoundException("Log not found: " + logId.getValue());
            }

            return OpenSearchLogEntryMapper.parseLogEntry(response.getHits().getHits()[0]);
        } catch (LogNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error finding log by ID: {}", logId.getValue(), e);
            throw new RuntimeException("Failed to find log", e);
        }
    }

    @Override
    public TraceLogResult executeTraceQuery(TraceLogQuery query) {
        try {
            SearchRequest searchRequest = new SearchRequest(indexName);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                    .must(QueryBuilders.termQuery("project_id", query.getProjectId().toString()))
                    .must(QueryBuilders.termQuery("trace_id", query.getTraceId()));
            
            sourceBuilder.query(boolQuery);
            sourceBuilder.sort("timestamp", SortOrder.ASC); // Trace logs sorted by timestamp
            sourceBuilder.size(10000); // Traces can have many logs
            searchRequest.source(sourceBuilder);

            SearchResponse response = openSearchClient.search(searchRequest, RequestOptions.DEFAULT);
            List<LogEntry> logs = parseLogEntries(response.getHits().getHits());

            return new TraceLogResult(query.getTraceId(), logs);
        } catch (Exception e) {
            log.error("Error executing trace query", e);
            throw new RuntimeException("Failed to execute trace query", e);
        }
    }

    private SearchRequest buildSearchRequest(LogQuery query) {
        SearchRequest searchRequest = new SearchRequest(indexName);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // Build query
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("project_id", query.getProjectId().toString()));

        // Time range filter
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

        // Full-text search
        query.getSearchText().ifPresent(searchText -> {
            boolQuery.must(QueryBuilders.matchQuery("message", searchText));
        });

        // Trace ID filter
        query.getTraceId().ifPresent(traceId -> {
            boolQuery.must(QueryBuilders.termQuery("trace_id", traceId));
        });

        sourceBuilder.query(boolQuery);

        // Sorting
        String sortField = query.getSortOrder().getField() == LogSortOrder.LogSortField.TIMESTAMP 
                ? "timestamp" 
                : "ingested_at";
        SortOrder sortOrder = query.getSortOrder().getDirection() == LogSortOrder.SortDirection.ASC
                ? SortOrder.ASC
                : SortOrder.DESC;
        sourceBuilder.sort(sortField, sortOrder);
        
        // Secondary sort by ingested_at for stable pagination
        if (sortField.equals("timestamp")) {
            sourceBuilder.sort("ingested_at", sortOrder);
        }
        sourceBuilder.sort("_id", sortOrder); // Tertiary sort for stable pagination

        // Pagination
        sourceBuilder.size(query.getLimit());
        query.getPaginationToken().ifPresent(token -> {
            try {
                // Decode search_after from token
                String decoded = new String(Base64.getDecoder().decode(token.getValue()));
                String[] parts = decoded.split("\\|");
                if (parts.length >= 2) {
                    Object[] searchAfter = new Object[parts.length];
                    searchAfter[0] = parts[0]; // timestamp or ingested_at
                    searchAfter[1] = parts[1]; // ingested_at (if timestamp was primary)
                    if (parts.length > 2) {
                        searchAfter[2] = parts[2]; // _id
                    }
                    sourceBuilder.searchAfter(searchAfter);
                }
            } catch (Exception e) {
                log.warn("Invalid pagination token, ignoring: {}", token.getValue(), e);
            }
        });

        sourceBuilder.timeout(TimeValue.timeValueSeconds(30));
        searchRequest.source(sourceBuilder);

        return searchRequest;
    }

    private List<LogEntry> parseLogEntries(SearchHit[] hits) {
        return OpenSearchLogEntryMapper.parseLogEntries(hits);
    }

    private LogPaginationToken extractPaginationToken(SearchResponse response, List<LogEntry> logs) {
        if (logs.isEmpty() || response.getHits().getHits().length < response.getHits().getTotalHits().value) {
            // There are more results
            SearchHit lastHit = response.getHits().getHits()[response.getHits().getHits().length - 1];
            Object[] searchAfter = lastHit.getSortValues();
            
            if (searchAfter != null && searchAfter.length > 0) {
                // Encode search_after array as base64 string
                StringBuilder tokenValue = new StringBuilder();
                for (int i = 0; i < searchAfter.length; i++) {
                    if (i > 0) tokenValue.append("|");
                    tokenValue.append(searchAfter[i] != null ? searchAfter[i].toString() : "");
                }
                String encoded = Base64.getEncoder().encodeToString(tokenValue.toString().getBytes());
                return new LogPaginationToken(encoded);
            }
        }
        return null; // No more results
    }
}


