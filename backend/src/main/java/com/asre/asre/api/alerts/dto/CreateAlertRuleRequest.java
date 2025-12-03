package com.asre.asre.api.alerts.dto;

import com.asre.asre.domain.alerts.AggregationStat;
import com.asre.asre.domain.alerts.ComparisonOperator;
import com.asre.asre.domain.alerts.IncidentSeverity;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAlertRuleRequest {
    @NotNull(message = "Service ID is required")
    private UUID serviceId;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Metric name is required")
    private String metricName;

    @NotNull(message = "Aggregation stat is required")
    private AggregationStat aggregationStat;

    @NotNull(message = "Operator is required")
    private ComparisonOperator operator;

    @NotNull(message = "Threshold is required")
    @Positive(message = "Threshold must be positive")
    private Double threshold;

    @NotNull(message = "Window minutes is required")
    @Min(value = 1, message = "Window minutes must be at least 1")
    private Integer windowMinutes;

    @NotNull(message = "Duration minutes is required")
    @Min(value = 1, message = "Duration minutes must be at least 1")
    private Integer durationMinutes;

    @NotNull(message = "Severity is required")
    private IncidentSeverity severity;

    private List<String> notificationChannels;
}

