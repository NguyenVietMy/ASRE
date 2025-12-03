package com.asre.asre.api.alerts.dto;

import com.asre.asre.domain.alerts.AggregationStat;
import com.asre.asre.domain.alerts.ComparisonOperator;
import com.asre.asre.domain.alerts.IncidentSeverity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertRuleResponse {
    private UUID id;
    private UUID projectId;
    private UUID serviceId;
    private String name;
    private String metricName;
    private AggregationStat aggregationStat;
    private ComparisonOperator operator;
    private Double threshold;
    private Integer windowMinutes;
    private Integer durationMinutes;
    private IncidentSeverity severity;
    private Boolean enabled;
    private List<String> notificationChannels;
    private Instant createdAt;
}

