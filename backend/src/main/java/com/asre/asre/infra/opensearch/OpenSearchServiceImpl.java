package com.asre.asre.infra.opensearch;

import com.asre.asre.domain.ingestion.LogIndexerPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.bulk.BulkResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenSearchServiceImpl implements LogIndexerPort {

    @Value("${opensearch.index.name:opensearch-logs}")
    private String indexName;

    private final RestHighLevelClient openSearchClient;

    @Override
    public void indexLog(com.asre.asre.domain.ingestion.LogEntry logEntry, String logId) {
        Map<String, Object> document = new HashMap<>();
        document.put("project_id", logEntry.getProjectId().toString());
        document.put("service_id", logEntry.getServiceId().toString());
        document.put("timestamp", logEntry.getTimestamp().toString());
        document.put("ingested_at", Instant.now().toString());
        document.put("level", logEntry.getLevel().name()); // Convert enum to string
        document.put("message", logEntry.getMessage());
        document.put("sampled", logEntry.isSampled()); // Store sampling flag
        if (logEntry.getTraceId() != null) {
            document.put("trace_id", logEntry.getTraceId());
        }
        if (logEntry.getContext() != null) {
            document.put("context", logEntry.getContext());
        }

        try {
            IndexRequest request = new IndexRequest(indexName)
                    .id(logId)
                    .source(document, XContentType.JSON);

            openSearchClient.index(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("Error indexing log to OpenSearch: logId={}", logId, e);
            throw new RuntimeException("Failed to index log to OpenSearch", e);
        }
    }

    @Override
    public void indexBatch(List<com.asre.asre.domain.ingestion.LogEntry> logEntries) {
        if (logEntries.isEmpty()) {
            return;
        }

        BulkRequest bulkRequest = new BulkRequest();

        for (com.asre.asre.domain.ingestion.LogEntry logEntry : logEntries) {
            String logId = UUID.randomUUID().toString();
            Map<String, Object> document = new HashMap<>();
            document.put("project_id", logEntry.getProjectId().toString());
            document.put("service_id", logEntry.getServiceId().toString());
            document.put("timestamp", logEntry.getTimestamp().toString());
            document.put("ingested_at", Instant.now().toString());
            document.put("level", logEntry.getLevel().name()); // Convert enum to string
            document.put("message", logEntry.getMessage());
            document.put("sampled", logEntry.isSampled()); // Store sampling flag
            if (logEntry.getTraceId() != null) {
                document.put("trace_id", logEntry.getTraceId());
            }
            if (logEntry.getContext() != null) {
                document.put("context", logEntry.getContext());
            }

            IndexRequest request = new IndexRequest(indexName)
                    .id(logId)
                    .source(document, XContentType.JSON);
            bulkRequest.add(request);
        }

        try {
            BulkResponse bulkResponse = openSearchClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            if (bulkResponse.hasFailures()) {
                log.error("OpenSearch bulk indexing had failures: {}", bulkResponse.buildFailureMessage());
            } else {
                log.debug("Indexed {} logs to OpenSearch", logEntries.size());
            }
        } catch (Exception e) {
            log.error("Error bulk indexing logs to OpenSearch", e);
            throw new RuntimeException("Failed to bulk index logs to OpenSearch", e);
        }
    }
}
