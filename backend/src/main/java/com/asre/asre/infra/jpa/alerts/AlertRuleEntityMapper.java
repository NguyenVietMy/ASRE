package com.asre.asre.infra.jpa.alerts;

import com.asre.asre.domain.alerts.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * Mapper between AlertRule domain object and AlertRuleEntity.
 */
@Component
public class AlertRuleEntityMapper {

    public AlertRule toDomain(AlertRuleEntity entity) {
        if (entity == null) {
            return null;
        }

        return AlertRule.builder()
                .id(entity.getId())
                .projectId(entity.getProjectId())
                .serviceId(entity.getServiceId())
                .name(entity.getName())
                .metricName(entity.getMetricName())
                .aggregationStat(entity.getAggregationStat() != null 
                    ? AggregationStat.fromString(entity.getAggregationStat()) 
                    : null)
                .operator(entity.getOperator() != null 
                    ? ComparisonOperator.fromString(entity.getOperator()) 
                    : null)
                .threshold(entity.getThreshold())
                .windowMinutes(entity.getWindowMinutes())
                .durationMinutes(entity.getDurationMinutes())
                .severity(entity.getSeverity() != null 
                    ? IncidentSeverity.valueOf(entity.getSeverity().toUpperCase()) 
                    : null)
                .enabled(entity.getEnabled())
                .notificationChannels(entity.getNotificationChannels() != null 
                    ? entity.getNotificationChannels() 
                    : new ArrayList<>())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public AlertRuleEntity toEntity(AlertRule domain) {
        if (domain == null) {
            return null;
        }

        return new AlertRuleEntity(
                domain.getId(),
                domain.getProjectId(),
                domain.getServiceId(),
                domain.getName(),
                domain.getMetricName(),
                domain.getAggregationStat() != null ? domain.getAggregationStat().name() : null,
                domain.getOperator() != null ? domain.getOperator().name() : null,
                domain.getThreshold(),
                domain.getWindowMinutes(),
                domain.getDurationMinutes(),
                domain.getSeverity() != null ? domain.getSeverity().name().toLowerCase() : null,
                domain.getEnabled(),
                domain.getNotificationChannels() != null ? domain.getNotificationChannels() : new ArrayList<>(),
                domain.getCreatedAt()
        );
    }
}

