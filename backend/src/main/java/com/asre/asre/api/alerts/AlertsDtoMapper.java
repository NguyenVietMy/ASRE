package com.asre.asre.api.alerts;

import com.asre.asre.api.alerts.dto.*;
import com.asre.asre.application.alerts.*;
import com.asre.asre.domain.alerts.AlertRule;
import com.asre.asre.domain.alerts.Incident;
import com.asre.asre.domain.alerts.IncidentEvent;
import com.asre.asre.domain.alerts.IncidentNote;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mapper between DTOs and domain/application objects.
 */
@Component
public class AlertsDtoMapper {

    public CreateAlertRuleCommand toCreateCommand(CreateAlertRuleRequest request, UUID projectId) {
        return CreateAlertRuleCommand.builder()
                .projectId(projectId)
                .serviceId(request.getServiceId())
                .name(request.getName())
                .metricName(request.getMetricName())
                .aggregationStat(request.getAggregationStat())
                .operator(request.getOperator())
                .threshold(request.getThreshold())
                .windowMinutes(request.getWindowMinutes())
                .durationMinutes(request.getDurationMinutes())
                .severity(request.getSeverity())
                .notificationChannels(request.getNotificationChannels())
                .build();
    }

    public UpdateAlertRuleCommand toUpdateCommand(UpdateAlertRuleRequest request, UUID ruleId, UUID projectId) {
        return UpdateAlertRuleCommand.builder()
                .ruleId(ruleId)
                .projectId(projectId)
                .name(request.getName())
                .metricName(request.getMetricName())
                .aggregationStat(request.getAggregationStat())
                .operator(request.getOperator())
                .threshold(request.getThreshold())
                .windowMinutes(request.getWindowMinutes())
                .durationMinutes(request.getDurationMinutes())
                .severity(request.getSeverity())
                .enabled(request.getEnabled())
                .notificationChannels(request.getNotificationChannels())
                .build();
    }

    public AlertRuleResponse toResponse(AlertRule rule) {
        return AlertRuleResponse.builder()
                .id(rule.getId())
                .projectId(rule.getProjectId())
                .serviceId(rule.getServiceId())
                .name(rule.getName())
                .metricName(rule.getMetricName())
                .aggregationStat(rule.getAggregationStat())
                .operator(rule.getOperator())
                .threshold(rule.getThreshold())
                .windowMinutes(rule.getWindowMinutes())
                .durationMinutes(rule.getDurationMinutes())
                .severity(rule.getSeverity())
                .enabled(rule.getEnabled())
                .notificationChannels(rule.getNotificationChannels())
                .createdAt(rule.getCreatedAt())
                .build();
    }

    public IncidentResponse toResponse(Incident incident) {
        return IncidentResponse.builder()
                .id(incident.getId())
                .projectId(incident.getProjectId())
                .serviceId(incident.getServiceId())
                .ruleId(incident.getRuleId())
                .status(incident.getStatus())
                .severity(incident.getSeverity())
                .startedAt(incident.getStartedAt())
                .resolvedAt(incident.getResolvedAt())
                .summary(incident.getSummary())
                .aiSummary(incident.getAiSummary())
                .createdAt(incident.getCreatedAt())
                .updatedAt(incident.getUpdatedAt())
                .build();
    }

    public AddIncidentNoteCommand toAddNoteCommand(AddIncidentNoteRequest request, UUID incidentId, 
                                                   UUID projectId, UUID authorUserId) {
        return AddIncidentNoteCommand.builder()
                .incidentId(incidentId)
                .projectId(projectId)
                .authorUserId(authorUserId)
                .content(request.getContent())
                .build();
    }

    public UpdateIncidentStatusCommand toUpdateStatusCommand(UpdateIncidentStatusRequest request, 
                                                             UUID incidentId, UUID projectId) {
        return UpdateIncidentStatusCommand.builder()
                .incidentId(incidentId)
                .projectId(projectId)
                .status(request.getStatus())
                .severity(request.getSeverity())
                .build();
    }

    public IncidentNoteResponse toResponse(IncidentNote note) {
        return IncidentNoteResponse.builder()
                .id(note.getId())
                .incidentId(note.getIncidentId())
                .authorUserId(note.getAuthorUserId())
                .content(note.getContent())
                .createdAt(note.getCreatedAt())
                .build();
    }

    public IncidentEventResponse toResponse(IncidentEvent event) {
        return IncidentEventResponse.builder()
                .id(event.getId())
                .incidentId(event.getIncidentId())
                .eventType(event.getEventType().name())
                .content(event.getContent())
                .timestamp(event.getTimestamp())
                .build();
    }
}

