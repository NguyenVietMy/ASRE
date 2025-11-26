package com.asre.asre.domain.ingestion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Metric {
    private UUID projectId;
    private UUID serviceId;
    private String metricName;
    private Double value;
    private Instant timestamp;
    private Map<String, String> tags;

    public boolean isValid() {
        return projectId != null
                && serviceId != null
                && metricName != null && !metricName.isBlank()
                && value != null
                && timestamp != null;
    }
}
