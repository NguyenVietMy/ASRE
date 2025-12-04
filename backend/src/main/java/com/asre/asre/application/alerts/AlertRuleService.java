package com.asre.asre.application.alerts;

import com.asre.asre.domain.alerts.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Application service for alert rule management.
 * Handles CRUD operations for alert rules.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertRuleService {

    private final AlertRuleRepository alertRuleRepository;
    private final MetricCatalogPort metricCatalog;

    /**
     * Creates a new alert rule.
     */
    @Transactional
    public AlertRule createAlertRule(CreateAlertRuleCommand command) {
        // Validate metric exists
        if (!metricCatalog.metricExists(command.getProjectId(), command.getMetricName())) {
            throw new InvalidAlertRuleException(
                    String.format("Metric '%s' does not exist for project %s",
                            command.getMetricName(), command.getProjectId()));
        }

        // Create alert rule using domain factory method
        AlertRule rule = AlertRule.create(
                command.getProjectId(),
                command.getServiceId(),
                command.getName(),
                command.getMetricName(),
                command.getAggregationStat(),
                command.getOperator(),
                command.getThreshold(),
                command.getWindowMinutes(),
                command.getDurationMinutes(),
                command.getSeverity(),
                command.getNotificationChannels());

        // Save
        return alertRuleRepository.save(rule);
    }

    /**
     * Updates an existing alert rule.
     */
    @Transactional
    public AlertRule updateAlertRule(UpdateAlertRuleCommand command) {
        AlertRule rule = alertRuleRepository.findById(command.getRuleId())
                .orElseThrow(() -> new IllegalArgumentException("Alert rule not found: " + command.getRuleId()));

        // Ensure belongs to project
        rule.ensureBelongsToProject(command.getProjectId());

        // Validate metric if changed
        if (command.getMetricName() != null && !command.getMetricName().equals(rule.getMetricName())) {
            if (!metricCatalog.metricExists(command.getProjectId(), command.getMetricName())) {
                throw new InvalidAlertRuleException(
                        String.format("Metric '%s' does not exist for project %s",
                                command.getMetricName(), command.getProjectId()));
            }
        }

        // Update using domain method
        AlertRule updatedRule = rule.updateFrom(command);

        // Save
        return alertRuleRepository.save(updatedRule);
    }

    /**
     * Gets an alert rule by ID.
     */
    public AlertRule getAlertRule(UUID ruleId, UUID projectId) {
        AlertRule rule = alertRuleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Alert rule not found: " + ruleId));
        rule.ensureBelongsToProject(projectId);
        return rule;
    }

    /**
     * Lists alert rules for a project.
     */
    public List<AlertRule> listAlertRules(UUID projectId) {
        return alertRuleRepository.findByProjectId(projectId);
    }

    /**
     * Lists alert rules for a service.
     */
    public List<AlertRule> listAlertRulesByService(UUID projectId, UUID serviceId) {
        List<AlertRule> rules = alertRuleRepository.findByServiceId(serviceId);
        // Filter by project for multi-tenant isolation
        return rules.stream()
                .filter(rule -> rule.getProjectId().equals(projectId))
                .toList();
    }

    /**
     * Deletes an alert rule.
     */
    @Transactional
    public void deleteAlertRule(UUID ruleId, UUID projectId) {
        AlertRule rule = alertRuleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Alert rule not found: " + ruleId));
        rule.ensureBelongsToProject(projectId);
        alertRuleRepository.delete(ruleId);
    }

    /**
     * Gets all active alert rules (for evaluation).
     */
    public List<AlertRule> getActiveRules() {
        return alertRuleRepository.findActiveRules();
    }

    /**
     * Disables alert rules for a service (when service is deleted).
     */
    @Transactional
    public void disableRulesForService(UUID serviceId) {
        List<AlertRule> rules = alertRuleRepository.findByServiceId(serviceId);
        rules.forEach(AlertRule::disable);
        rules.forEach(alertRuleRepository::save);
    }
}
