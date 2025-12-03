package com.asre.asre.domain.alerts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Value object representing the current state of an alert evaluation.
 * Tracks firing state and duration for "for duration" logic.
 * Storage-specific concerns (like Redis key formatting) are handled in
 * infrastructure layer.
 */
@Getter
@Builder
@AllArgsConstructor
public class AlertState {
    private final UUID projectId;
    private final UUID ruleId;
    private final UUID serviceId;
    private final boolean firing;
    private final Instant firstTriggerTime;
    private final Instant lastEvaluationTime;
    private final int consecutiveFiringCount;

    /**
     * Creates a new alert state with condition met (firing).
     */
    public AlertState withConditionMet(Instant evaluationTime, int currentCount) {
        return AlertState.builder()
                .projectId(this.projectId)
                .ruleId(this.ruleId)
                .serviceId(this.serviceId)
                .firing(true)
                .firstTriggerTime(this.firstTriggerTime != null ? this.firstTriggerTime : evaluationTime)
                .lastEvaluationTime(evaluationTime)
                .consecutiveFiringCount(currentCount + 1)
                .build();
    }

    /**
     * Creates a new alert state with condition not met (not firing).
     */
    public AlertState withConditionNotMet(Instant evaluationTime) {
        return AlertState.builder()
                .projectId(this.projectId)
                .ruleId(this.ruleId)
                .serviceId(this.serviceId)
                .firing(false)
                .firstTriggerTime(null)
                .lastEvaluationTime(evaluationTime)
                .consecutiveFiringCount(0)
                .build();
    }

    /**
     * Creates an initial alert state.
     */
    public static AlertState initial(UUID projectId, UUID ruleId, UUID serviceId, Instant evaluationTime) {
        return AlertState.builder()
                .projectId(projectId)
                .ruleId(ruleId)
                .serviceId(serviceId)
                .firing(false)
                .firstTriggerTime(null)
                .lastEvaluationTime(evaluationTime)
                .consecutiveFiringCount(0)
                .build();
    }
}
