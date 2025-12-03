package com.asre.asre.application.alerts;

import com.asre.asre.application.metrics.MetricsQueryService;
import com.asre.asre.domain.alerts.*;
import com.asre.asre.domain.metrics.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service for alert rule evaluation.
 * Evaluates rules, updates alert state, and creates/updates incidents.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertEvaluationService {

    private final AlertRuleRepository alertRuleRepository;
    private final AlertStateRepository alertStateRepository;
    private final IncidentRepository incidentRepository;
    private final IncidentService incidentService;
    private final MetricsQueryService metricsQueryService;
    private final NotificationService notificationService;

    /**
     * Evaluates a single alert rule.
     */
    public void evaluateRule(EvaluateRuleCommand command) {
        AlertRule rule = alertRuleRepository.findById(command.getRuleId())
                .orElseThrow(() -> new IllegalArgumentException("Alert rule not found: " + command.getRuleId()));

        if (!rule.isActive()) {
            log.debug("Skipping evaluation of disabled rule: {}", rule.getId());
            return;
        }

        try {
            // Query metrics for the evaluation window
            Instant now = command.getEvaluationTime() != null 
                ? command.getEvaluationTime() 
                : Instant.now();
            Instant windowStart = now.minus(rule.getWindowMinutes(), ChronoUnit.MINUTES);

            TimeRange timeRange = new TimeRange(windowStart, now);
            RollupPeriod rollupPeriod = new RollupPeriod(Duration.ofMinutes(rule.getWindowMinutes()));
            
            // Map AggregationStat to AggregationType
            AggregationType aggregationType = mapAggregationStat(rule.getAggregationStat());

            MetricQuery query = new MetricQuery(
                    command.getProjectId(),
                    rule.getMetricName(),
                    aggregationType,
                    timeRange,
                    rollupPeriod,
                    command.getServiceId(),
                    null
            );

            MetricQueryResult result = metricsQueryService.queryMetrics(query);

            // Evaluate condition
            boolean conditionMet = evaluateCondition(result, rule);

            // Update alert state
            AlertState state = updateAlertState(command.getProjectId(), command.getRuleId(), 
                    command.getServiceId(), conditionMet, now, rule.getDurationMinutes());

            // Check if we should create/update incident
            if (shouldCreateIncident(state, rule)) {
                handleIncidentCreation(command.getProjectId(), command.getServiceId(), 
                        command.getRuleId(), rule, result, state);
            } else if (!conditionMet && state.isFiring()) {
                // Condition cleared - resolve incident if exists
                handleIncidentResolution(command.getProjectId(), command.getRuleId(), command.getServiceId());
            }

        } catch (Exception e) {
            log.error("Failed to evaluate alert rule {}: {}", rule.getId(), e.getMessage(), e);
            // Do not update state on evaluation failure
            throw new RuntimeException("Alert evaluation failed", e);
        }
    }

    /**
     * Maps AggregationStat to AggregationType.
     */
    private AggregationType mapAggregationStat(AggregationStat stat) {
        return switch (stat) {
            case AVG -> AggregationType.AVG;
            case P95 -> AggregationType.P95;
            case P99 -> AggregationType.P99;
            case MAX -> AggregationType.MAX;
            case MIN -> AggregationType.MIN;
        };
    }

    /**
     * Evaluates the alert condition against the metric result.
     */
    private boolean evaluateCondition(MetricQueryResult result, AlertRule rule) {
        if (result.getDataPoints().isEmpty()) {
            log.debug("No data points for metric {} in evaluation window", rule.getMetricName());
            return false; // No data = condition not met
        }

        // For alert evaluation, we typically want the most recent aggregated value
        // or the average across the window
        double value = result.getDataPoints().stream()
                .mapToDouble(TimeSeriesPoint::getValue)
                .average()
                .orElse(0.0);

        return rule.getOperator().evaluate(value, rule.getThreshold());
    }

    /**
     * Updates alert state in Redis.
     */
    private AlertState updateAlertState(UUID projectId, UUID ruleId, UUID serviceId,
                                       boolean conditionMet, Instant now, int durationMinutes) {
        Optional<AlertState> existingState = alertStateRepository.findByKey(projectId, ruleId, serviceId);

        AlertState state;
        if (existingState.isPresent()) {
            state = existingState.get();
        } else {
            state = AlertState.initial(projectId, ruleId, serviceId, now);
        }

        // Use domain methods to create new state
        AlertState newState;
        if (conditionMet) {
            if (!state.isFiring()) {
                // Transition from not-firing to firing
                newState = state.withConditionMet(now, 0);
            } else {
                // Continue firing - increment count
                newState = state.withConditionMet(now, state.getConsecutiveFiringCount());
            }
        } else {
            // Condition not met - reset
            newState = state.withConditionNotMet(now);
        }

        // Calculate TTL: max(window_minutes, duration_minutes) * 3, or 1-2 hours
        int ttlSeconds = Math.max(durationMinutes * 3 * 60, 3600); // At least 1 hour
        alertStateRepository.save(newState, ttlSeconds);

        return newState;
    }

    /**
     * Checks if an incident should be created based on duration logic.
     */
    private boolean shouldCreateIncident(AlertState state, AlertRule rule) {
        if (!state.isFiring()) {
            return false;
        }

        // Check if condition has been firing for duration_minutes
        // We evaluate every minute, so consecutive_firing_count * 1 minute >= duration_minutes
        return state.getConsecutiveFiringCount() >= rule.getDurationMinutes();
    }

    /**
     * Handles incident creation when alert fires.
     */
    private void handleIncidentCreation(UUID projectId, UUID serviceId, UUID ruleId,
                                        AlertRule rule, MetricQueryResult result, AlertState state) {
        // Use findOrCreateOpenIncident which handles the logic
        String summary = String.format("Alert rule '%s' triggered: %s %s %.2f (threshold: %.2f)",
                rule.getName(),
                rule.getMetricName(),
                rule.getOperator().getSymbol(),
                result.getDataPoints().isEmpty() ? 0.0 : result.getDataPoints().get(0).getValue(),
                rule.getThreshold());

        Incident incident = incidentService.findOrCreateOpenIncident(
                projectId, serviceId, ruleId, rule.getSeverity(), summary);

        if (incident.getCreatedAt().equals(incident.getUpdatedAt())) {
            // New incident created
            log.info("Created incident {} for rule {}", incident.getId(), ruleId);
            notificationService.requestNotification(projectId, ruleId, serviceId, 
                    incident.getId(), rule.getNotificationChannels(), "INCIDENT_CREATED");
        } else {
            // Existing incident updated
            log.debug("Updated existing incident {} for rule {}", incident.getId(), ruleId);
        }
    }

    /**
     * Handles incident resolution when alert condition clears.
     */
    private void handleIncidentResolution(UUID projectId, UUID ruleId, UUID serviceId) {
        // Find open incident
        Optional<Incident> incidentOpt = incidentRepository.findOpenIncident(projectId, ruleId, serviceId);

        if (incidentOpt.isPresent()) {
            Incident incident = incidentOpt.get();
            UpdateIncidentStatusCommand command = UpdateIncidentStatusCommand.builder()
                    .incidentId(incident.getId())
                    .projectId(projectId)
                    .status(IncidentStatus.RESOLVED)
                    .build();
            incidentService.updateIncidentStatus(command);

            log.info("Resolved incident {} for rule {}", incident.getId(), ruleId);

            // Request notification
            AlertRule rule = alertRuleRepository.findById(ruleId).orElse(null);
            if (rule != null) {
                notificationService.requestNotification(projectId, ruleId, serviceId,
                        incident.getId(), rule.getNotificationChannels(), "INCIDENT_RESOLVED");
            }
        }
    }
}

