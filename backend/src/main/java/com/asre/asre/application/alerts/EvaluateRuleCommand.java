package com.asre.asre.application.alerts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Command to evaluate a single alert rule.
 * Used by the alert evaluation worker.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluateRuleCommand {
    private UUID ruleId;
    private UUID projectId;
    private UUID serviceId;
    private String metricName;
    private Instant evaluationTime;
}

