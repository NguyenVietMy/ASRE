package com.asre.asre.domain.alerts;

import com.asre.asre.application.alerts.UpdateAlertRuleCommand;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Alert rule aggregate.
 * Represents a rule that evaluates metrics and triggers incidents.
 * Enforces business invariants through domain methods.
 */
@Getter
@Builder
public class AlertRule {
    private final UUID id;
    private final UUID projectId;
    private final UUID serviceId;
    private final String name;
    private final String metricName;
    private final AggregationStat aggregationStat;
    private final ComparisonOperator operator;
    private final Double threshold;
    private final Integer windowMinutes;
    private final Integer durationMinutes;
    private final IncidentSeverity severity;
    private Boolean enabled;
    private final List<String> notificationChannels; // email addresses, webhook URLs
    private final Instant createdAt;

    /**
     * Creates a new alert rule.
     */
    public static AlertRule create(UUID projectId, UUID serviceId, String name, String metricName,
            AggregationStat aggregationStat, ComparisonOperator operator,
            Double threshold, Integer windowMinutes, Integer durationMinutes,
            IncidentSeverity severity, List<String> notificationChannels) {
        AlertRule rule = AlertRule.builder()
                .projectId(projectId)
                .serviceId(serviceId)
                .name(name)
                .metricName(metricName)
                .aggregationStat(aggregationStat)
                .operator(operator)
                .threshold(threshold)
                .windowMinutes(windowMinutes)
                .durationMinutes(durationMinutes)
                .severity(severity)
                .enabled(true)
                .notificationChannels(notificationChannels != null ? notificationChannels : List.of())
                .createdAt(Instant.now())
                .build();

        rule.validate();
        return rule;
    }

    /**
     * Updates the alert rule from a command.
     * Returns a new instance with updated fields.
     */
    public AlertRule updateFrom(UpdateAlertRuleCommand command) {
        validateUpdateCommand(command);

        return AlertRule.builder()
                .id(this.id)
                .projectId(this.projectId)
                .serviceId(this.serviceId)
                .name(command.getName() != null ? command.getName() : this.name)
                .metricName(command.getMetricName() != null ? command.getMetricName() : this.metricName)
                .aggregationStat(
                        command.getAggregationStat() != null ? command.getAggregationStat() : this.aggregationStat)
                .operator(command.getOperator() != null ? command.getOperator() : this.operator)
                .threshold(command.getThreshold() != null ? command.getThreshold() : this.threshold)
                .windowMinutes(command.getWindowMinutes() != null ? command.getWindowMinutes() : this.windowMinutes)
                .durationMinutes(
                        command.getDurationMinutes() != null ? command.getDurationMinutes() : this.durationMinutes)
                .severity(command.getSeverity() != null ? command.getSeverity() : this.severity)
                .enabled(command.getEnabled() != null ? command.getEnabled() : this.enabled)
                .notificationChannels(command.getNotificationChannels() != null ? command.getNotificationChannels()
                        : this.notificationChannels)
                .createdAt(this.createdAt)
                .build();
    }

    /**
     * Validates the alert rule.
     * 
     * @throws InvalidAlertRuleException if the rule is invalid
     */
    private void validate() {
        if (name == null || name.isBlank()) {
            throw new InvalidAlertRuleException("Alert rule name cannot be empty");
        }
        if (metricName == null || metricName.isBlank()) {
            throw new InvalidAlertRuleException("Metric name cannot be empty");
        }
        if (aggregationStat == null) {
            throw new InvalidAlertRuleException("Aggregation stat is required");
        }
        if (operator == null) {
            throw new InvalidAlertRuleException("Comparison operator is required");
        }
        if (threshold == null) {
            throw new InvalidAlertRuleException("Threshold is required");
        }
        if (windowMinutes == null || windowMinutes <= 0) {
            throw new InvalidAlertRuleException("Window minutes must be positive");
        }
        if (durationMinutes == null || durationMinutes <= 0) {
            throw new InvalidAlertRuleException("Duration minutes must be positive");
        }
        if (severity == null) {
            throw new InvalidAlertRuleException("Severity is required");
        }
        if (projectId == null) {
            throw new InvalidAlertRuleException("Project ID is required");
        }
        if (serviceId == null) {
            throw new InvalidAlertRuleException("Service ID is required");
        }
    }

    private void validateUpdateCommand(UpdateAlertRuleCommand command) {
        if (command.getName() != null && command.getName().isBlank()) {
            throw new InvalidAlertRuleException("Alert rule name cannot be empty");
        }
        if (command.getMetricName() != null && command.getMetricName().isBlank()) {
            throw new InvalidAlertRuleException("Metric name cannot be empty");
        }
        if (command.getWindowMinutes() != null && command.getWindowMinutes() <= 0) {
            throw new InvalidAlertRuleException("Window minutes must be positive");
        }
        if (command.getDurationMinutes() != null && command.getDurationMinutes() <= 0) {
            throw new InvalidAlertRuleException("Duration minutes must be positive");
        }
        if (command.getThreshold() != null && command.getThreshold() <= 0) {
            throw new InvalidAlertRuleException("Threshold must be positive");
        }
    }

    /**
     * Ensures the alert rule belongs to the specified project.
     */
    public void ensureBelongsToProject(UUID projectId) {
        if (!this.projectId.equals(projectId)) {
            throw new InvalidAlertRuleException("Alert rule does not belong to project: " + projectId);
        }
    }

    /**
     * Checks if the rule is active (enabled).
     */
    public boolean isActive() {
        return enabled != null && enabled;
    }

    /**
     * Disables the rule (e.g., when service is deleted).
     */
    public void disable() {
        this.enabled = false;
    }
}
