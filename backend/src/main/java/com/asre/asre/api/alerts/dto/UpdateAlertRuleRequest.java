package com.asre.asre.api.alerts.dto;

import com.asre.asre.domain.alerts.AggregationStat;
import com.asre.asre.domain.alerts.ComparisonOperator;
import com.asre.asre.domain.alerts.IncidentSeverity;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAlertRuleRequest {
    private String name;
    private String metricName;
    private AggregationStat aggregationStat;
    private ComparisonOperator operator;
    private Double threshold;
    
    @Min(value = 1, message = "Window minutes must be at least 1")
    private Integer windowMinutes;
    
    @Min(value = 1, message = "Duration minutes must be at least 1")
    private Integer durationMinutes;
    
    private IncidentSeverity severity;
    private Boolean enabled;
    private List<String> notificationChannels;
}

