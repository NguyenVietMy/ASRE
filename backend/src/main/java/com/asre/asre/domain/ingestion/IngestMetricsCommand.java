package com.asre.asre.domain.ingestion;

import lombok.Value;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Value
public class IngestMetricsCommand {
    UUID projectId;
    List<MetricData> metrics;

    @Value
    public static class MetricData {
        UUID serviceId;
        String metricName;
        Double value;
        String timestamp;
        Map<String, String> tags;
    }
}


