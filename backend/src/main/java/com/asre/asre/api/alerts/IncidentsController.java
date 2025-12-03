package com.asre.asre.api.alerts;

import com.asre.asre.api.alerts.dto.*;
import com.asre.asre.application.alerts.AddIncidentNoteCommand;
import com.asre.asre.application.alerts.IncidentService;
import com.asre.asre.application.alerts.UpdateIncidentStatusCommand;
import com.asre.asre.domain.alerts.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
@Slf4j
public class IncidentsController {

    private final IncidentService incidentService;
    private final AlertsDtoMapper mapper;

    @GetMapping
    public ResponseEntity<?> listIncidents(
            @RequestHeader("X-Project-ID") String projectIdHeader,
            @RequestParam(required = false) IncidentStatus status,
            @RequestParam(required = false) IncidentSeverity severity,
            @RequestParam(required = false) UUID serviceId,
            @RequestParam(required = false) UUID ruleId) {
        try {
            UUID projectId = UUID.fromString(projectIdHeader);
            List<Incident> incidents = incidentService.listIncidents(projectId, status, severity, serviceId, ruleId);
            
            List<IncidentResponse> responses = incidents.stream()
                    .map(mapper::toResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error listing incidents", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to list incidents"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getIncident(
            @PathVariable UUID id,
            @RequestHeader("X-Project-ID") String projectIdHeader) {
        try {
            UUID projectId = UUID.fromString(projectIdHeader);
            Incident incident = incidentService.getIncident(id, projectId);
            return ResponseEntity.ok(mapper.toResponse(incident));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error getting incident", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get incident"));
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateIncidentStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateIncidentStatusRequest request,
            @RequestHeader("X-Project-ID") String projectIdHeader) {
        try {
            UUID projectId = UUID.fromString(projectIdHeader);
            UpdateIncidentStatusCommand command = mapper.toUpdateStatusCommand(request, id, projectId);
            Incident incident = incidentService.updateIncidentStatus(command);
            return ResponseEntity.ok(mapper.toResponse(incident));
        } catch (IllegalIncidentTransitionException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating incident status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update incident status"));
        }
    }

    @PostMapping("/{id}/notes")
    public ResponseEntity<?> addNote(
            @PathVariable UUID id,
            @Valid @RequestBody AddIncidentNoteRequest request,
            @RequestHeader("X-Project-ID") String projectIdHeader,
            @RequestHeader("X-User-ID") String userIdHeader) {
        try {
            UUID projectId = UUID.fromString(projectIdHeader);
            UUID userId = UUID.fromString(userIdHeader);
            AddIncidentNoteCommand command = mapper.toAddNoteCommand(request, id, projectId, userId);
            IncidentNote note = incidentService.addNote(command);
            return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(note));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error adding note to incident", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to add note"));
        }
    }

    @GetMapping("/{id}/notes")
    public ResponseEntity<?> getNotes(
            @PathVariable UUID id,
            @RequestHeader("X-Project-ID") String projectIdHeader) {
        try {
            UUID projectId = UUID.fromString(projectIdHeader);
            List<IncidentNote> notes = incidentService.getNotes(id, projectId);
            
            List<IncidentNoteResponse> responses = notes.stream()
                    .map(mapper::toResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error getting incident notes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get notes"));
        }
    }

    @GetMapping("/{id}/timeline")
    public ResponseEntity<?> getTimeline(
            @PathVariable UUID id,
            @RequestHeader("X-Project-ID") String projectIdHeader) {
        try {
            UUID projectId = UUID.fromString(projectIdHeader);
            List<IncidentEvent> events = incidentService.getTimeline(id, projectId);
            
            List<IncidentEventResponse> responses = events.stream()
                    .map(mapper::toResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error getting incident timeline", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get timeline"));
        }
    }
}

