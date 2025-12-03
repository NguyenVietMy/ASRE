package com.asre.asre.application.alerts;

import com.asre.asre.domain.alerts.AggregationStat;
import com.asre.asre.domain.alerts.ComparisonOperator;
import com.asre.asre.domain.alerts.IncidentSeverity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Command to update an existing alert rule.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAlertRuleCommand {
    private UUID ruleId;
    private UUID projectId;
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
}

