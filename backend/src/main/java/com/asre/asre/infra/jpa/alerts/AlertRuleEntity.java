package com.asre.asre.infra.jpa.alerts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Entity representation for AlertRule - persistence layer only.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertRuleEntity {
    private UUID id;
    private UUID projectId;
    private UUID serviceId;
    private String name;
    private String metricName;
    private String aggregationStat; // Stored as string
    private String operator; // Stored as string
    private Double threshold;
    private Integer windowMinutes;
    private Integer durationMinutes;
    private String severity; // Stored as string
    private Boolean enabled;
    private List<String> notificationChannels; // Stored as JSONB array
    private Instant createdAt;
}

