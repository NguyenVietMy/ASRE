package com.asre.asre.infra.jpa.alerts;

import com.asre.asre.domain.alerts.Incident;
import com.asre.asre.domain.alerts.IncidentSeverity;
import com.asre.asre.domain.alerts.IncidentStatus;
import org.springframework.stereotype.Component;

/**
 * Mapper between Incident domain object and IncidentEntity.
 */
@Component
public class IncidentEntityMapper {

    public Incident toDomain(IncidentEntity entity) {
        if (entity == null) {
            return null;
        }

        return Incident.builder()
                .id(entity.getId())
                .projectId(entity.getProjectId())
                .serviceId(entity.getServiceId())
                .ruleId(entity.getRuleId())
                .status(entity.getStatus() != null ? IncidentStatus.valueOf(entity.getStatus().toUpperCase()) : null)
                .severity(entity.getSeverity() != null ? IncidentSeverity.valueOf(entity.getSeverity()) : null)
                .startedAt(entity.getStartedAt())
                .resolvedAt(entity.getResolvedAt())
                .summary(entity.getSummary())
                .aiSummary(entity.getAiSummary())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public IncidentEntity toEntity(Incident domain) {
        if (domain == null) {
            return null;
        }

        return new IncidentEntity(
                domain.getId(),
                domain.getProjectId(),
                domain.getServiceId(),
                domain.getRuleId(),
                domain.getStatus() != null ? domain.getStatus().name().toLowerCase() : null,
                domain.getSeverity() != null ? domain.getSeverity().name().toLowerCase() : null,
                domain.getStartedAt(),
                domain.getResolvedAt(),
                domain.getSummary(),
                domain.getAiSummary(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}

