package com.asre.asre.infra.opensearch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.indices.CreateIndexRequest;
import org.opensearch.client.indices.CreateIndexResponse;
import org.opensearch.client.indices.GetIndexRequest;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpenSearchIndexInitializer implements CommandLineRunner {

    @Value("${opensearch.index.name:opensearch-logs}")
    private String indexName;

    private final RestHighLevelClient openSearchClient;

    @Override
    public void run(String... args) throws Exception {
        try {
            // Check if index exists
            GetIndexRequest existsRequest = new GetIndexRequest(indexName);
            boolean exists = openSearchClient.indices().exists(existsRequest, RequestOptions.DEFAULT);

            if (exists) {
                log.info("OpenSearch index '{}' already exists", indexName);
                return;
            }

            // Create index with mappings
            CreateIndexRequest createRequest = new CreateIndexRequest(indexName);
            createRequest.settings(Settings.builder()
                    .put("index.number_of_shards", 1)
                    .put("index.number_of_replicas", 0)
            );

            String mapping = """
                    {
                      "properties": {
                        "project_id": {
                          "type": "keyword"
                        },
                        "service_id": {
                          "type": "keyword"
                        },
                        "timestamp": {
                          "type": "date"
                        },
                        "ingested_at": {
                          "type": "date"
                        },
                        "level": {
                          "type": "keyword"
                        },
                        "message": {
                          "type": "text",
                          "analyzer": "standard",
                          "fields": {
                            "keyword": {
                              "type": "keyword"
                            }
                          }
                        },
                        "trace_id": {
                          "type": "keyword"
                        },
                        "context": {
                          "type": "object"
                        }
                      }
                    }
                    """;

            createRequest.mapping(mapping, XContentType.JSON);

            CreateIndexResponse createResponse = openSearchClient.indices().create(createRequest, RequestOptions.DEFAULT);
            if (createResponse.isAcknowledged()) {
                log.info("Successfully created OpenSearch index '{}'", indexName);
            } else {
                log.warn("OpenSearch index creation was not acknowledged");
            }
        } catch (Exception e) {
            log.error("Error initializing OpenSearch index", e);
            // Don't fail startup if OpenSearch is not available
        }
    }
}

