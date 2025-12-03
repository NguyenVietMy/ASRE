package com.asre.asre.application.alerts;

import com.asre.asre.domain.alerts.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Application service for incident management.
 * Handles CRUD operations and lifecycle management for incidents.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final IncidentEventRepository eventRepository;
    private final IncidentNoteRepository noteRepository;

    /**
     * Creates a new incident.
     */
    @Transactional
    public Incident createIncident(UUID projectId, UUID serviceId, UUID ruleId, 
                                   IncidentSeverity severity, String summary) {
        // Create using domain factory method
        Incident incident = Incident.create(projectId, serviceId, ruleId, severity, summary);

        Incident saved = incidentRepository.save(incident);

        // Create initial event
        IncidentEvent event = IncidentEvent.builder()
                .incidentId(saved.getId())
                .eventType(IncidentEvent.IncidentEventType.RULE_TRIGGERED)
                .timestamp(Instant.now())
                .build();
        eventRepository.save(event);

        return saved;
    }

    /**
     * Gets an incident by ID.
     */
    public Incident getIncident(UUID incidentId, UUID projectId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found: " + incidentId));
        incident.ensureBelongsToProject(projectId);
        return incident;
    }

    /**
     * Lists incidents for a project with optional filters.
     */
    public List<Incident> listIncidents(UUID projectId, IncidentStatus status, 
                                       IncidentSeverity severity, UUID serviceId, UUID ruleId) {
        List<Incident> incidents = incidentRepository.findByProjectId(projectId);

        // Apply filters
        if (status != null) {
            incidents = incidents.stream()
                    .filter(i -> i.getStatus() == status)
                    .toList();
        }
        if (severity != null) {
            incidents = incidents.stream()
                    .filter(i -> i.getSeverity() == severity)
                    .toList();
        }
        if (serviceId != null) {
            incidents = incidents.stream()
                    .filter(i -> i.getServiceId().equals(serviceId))
                    .toList();
        }
        if (ruleId != null) {
            incidents = incidents.stream()
                    .filter(i -> i.getRuleId() != null && i.getRuleId().equals(ruleId))
                    .toList();
        }

        return incidents;
    }

    /**
     * Updates incident status.
     */
    @Transactional
    public Incident updateIncidentStatus(UpdateIncidentStatusCommand command) {
        Incident incident = incidentRepository.findById(command.getIncidentId())
                .orElseThrow(() -> new IllegalArgumentException("Incident not found: " + command.getIncidentId()));
        
        incident.ensureBelongsToProject(command.getProjectId());

        // Update using domain method
        incident.updateStatus(command.getStatus(), command.getSeverity());

        Incident saved = incidentRepository.save(incident);

        // Create status change event
        if (command.getStatus() != null) {
            IncidentEvent event = IncidentEvent.builder()
                    .incidentId(saved.getId())
                    .eventType(IncidentEvent.IncidentEventType.STATUS_CHANGED)
                    .content(java.util.Map.of(
                        "oldStatus", incident.getStatus().name(),
                        "newStatus", command.getStatus().name()
                    ))
                    .timestamp(Instant.now())
                    .build();
            eventRepository.save(event);
        }

        return saved;
    }

    /**
     * Adds a note to an incident.
     */
    @Transactional
    public IncidentNote addNote(AddIncidentNoteCommand command) {
        Incident incident = incidentRepository.findById(command.getIncidentId())
                .orElseThrow(() -> new IllegalArgumentException("Incident not found: " + command.getIncidentId()));
        
        incident.ensureBelongsToProject(command.getProjectId());

        IncidentNote note = IncidentNote.builder()
                .incidentId(command.getIncidentId())
                .authorUserId(command.getAuthorUserId())
                .content(command.getContent())
                .createdAt(Instant.now())
                .build();

        IncidentNote saved = noteRepository.save(note);

        // Create comment event
        IncidentEvent event = IncidentEvent.builder()
                .incidentId(command.getIncidentId())
                .eventType(IncidentEvent.IncidentEventType.COMMENT)
                .content(java.util.Map.of("noteId", saved.getId().toString()))
                .timestamp(Instant.now())
                .build();
        eventRepository.save(event);

        return saved;
    }

    /**
     * Gets the timeline (events) for an incident.
     */
    public List<IncidentEvent> getTimeline(UUID incidentId, UUID projectId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found: " + incidentId));
        incident.ensureBelongsToProject(projectId);
        return eventRepository.findByIncidentId(incidentId);
    }

    /**
     * Gets notes for an incident.
     */
    public List<IncidentNote> getNotes(UUID incidentId, UUID projectId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found: " + incidentId));
        incident.ensureBelongsToProject(projectId);
        return noteRepository.findByIncidentId(incidentId);
    }

    /**
     * Finds or creates an open incident for a (project, rule, service) combination.
     */
    @Transactional
    public Incident findOrCreateOpenIncident(UUID projectId, UUID serviceId, UUID ruleId,
                                            IncidentSeverity severity, String summary) {
        return incidentRepository.findOpenIncident(projectId, ruleId, serviceId)
                .orElseGet(() -> createIncident(projectId, serviceId, ruleId, severity, summary));
    }

    /**
     * Updates an existing open incident (e.g., when alert continues firing).
     */
    @Transactional
    public Incident updateOpenIncident(UUID incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IllegalArgumentException("Incident not found: " + incidentId));
        
        if (!incident.isOpen()) {
            throw new IllegalStateException("Cannot update resolved incident");
        }

        // Update timestamp using domain method
        incident.touch();
        return incidentRepository.save(incident);
    }
}

