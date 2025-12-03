package com.asre.asre.api.alerts;

import com.asre.asre.api.alerts.dto.*;
import com.asre.asre.application.alerts.AlertRuleService;
import com.asre.asre.application.alerts.CreateAlertRuleCommand;
import com.asre.asre.application.alerts.UpdateAlertRuleCommand;
import com.asre.asre.domain.alerts.AlertRule;
import com.asre.asre.domain.alerts.InvalidAlertRuleException;
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
@RequestMapping("/api/alerts/rules")
@RequiredArgsConstructor
@Slf4j
public class AlertRulesController {

    private final AlertRuleService alertRuleService;
    private final AlertsDtoMapper mapper;

    @PostMapping
    public ResponseEntity<?> createAlertRule(
            @Valid @RequestBody CreateAlertRuleRequest request,
            @RequestHeader("X-Project-ID") String projectIdHeader) {
        try {
            UUID projectId = UUID.fromString(projectIdHeader);
            CreateAlertRuleCommand command = mapper.toCreateCommand(request, projectId);
            AlertRule rule = alertRuleService.createAlertRule(command);
            return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(rule));
        } catch (InvalidAlertRuleException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating alert rule", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create alert rule"));
        }
    }

    @GetMapping
    public ResponseEntity<?> listAlertRules(
            @RequestHeader("X-Project-ID") String projectIdHeader,
            @RequestParam(required = false) UUID serviceId) {
        try {
            UUID projectId = UUID.fromString(projectIdHeader);
            List<AlertRule> rules;
            
            if (serviceId != null) {
                rules = alertRuleService.listAlertRulesByService(projectId, serviceId);
            } else {
                rules = alertRuleService.listAlertRules(projectId);
            }
            
            List<AlertRuleResponse> responses = rules.stream()
                    .map(mapper::toResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error listing alert rules", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to list alert rules"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAlertRule(
            @PathVariable UUID id,
            @RequestHeader("X-Project-ID") String projectIdHeader) {
        try {
            UUID projectId = UUID.fromString(projectIdHeader);
            AlertRule rule = alertRuleService.getAlertRule(id, projectId);
            return ResponseEntity.ok(mapper.toResponse(rule));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error getting alert rule", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get alert rule"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAlertRule(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAlertRuleRequest request,
            @RequestHeader("X-Project-ID") String projectIdHeader) {
        try {
            UUID projectId = UUID.fromString(projectIdHeader);
            UpdateAlertRuleCommand command = mapper.toUpdateCommand(request, id, projectId);
            AlertRule rule = alertRuleService.updateAlertRule(command);
            return ResponseEntity.ok(mapper.toResponse(rule));
        } catch (InvalidAlertRuleException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating alert rule", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update alert rule"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAlertRule(
            @PathVariable UUID id,
            @RequestHeader("X-Project-ID") String projectIdHeader) {
        try {
            UUID projectId = UUID.fromString(projectIdHeader);
            alertRuleService.deleteAlertRule(id, projectId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting alert rule", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete alert rule"));
        }
    }
}

