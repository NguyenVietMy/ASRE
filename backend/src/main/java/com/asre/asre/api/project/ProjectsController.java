package com.asre.asre.api.project;

import com.asre.asre.api.project.dto.*;
import com.asre.asre.application.project.CreateProjectCommand;
import com.asre.asre.application.project.ProjectService;
import com.asre.asre.application.project.UpdateProjectCommand;
import com.asre.asre.domain.project.Project;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Slf4j
public class ProjectsController {

    private final ProjectService projectService;
    private final ProjectsDtoMapper mapper;

    /**
     * Extract userId from JWT authentication.
     */
    private UUID getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("User not authenticated");
        }
        return UUID.fromString(authentication.getName());
    }

    @PostMapping
    public ResponseEntity<?> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            Authentication authentication) {
        try {
            UUID userId = getCurrentUserId(authentication);
            CreateProjectCommand command = new CreateProjectCommand(
                    request.getName(),
                    request.getDescription(),
                    userId);
            Project project = projectService.createProject(command);
            ProjectResponse response = mapper.toResponse(project);

            // Include API key in response (only shown once at creation)
            ApiKeyResponse apiKeyResponse = new ApiKeyResponse(
                    project.getApiKey(),
                    mapper.maskApiKey(project.getApiKey()));

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "project", response,
                            "apiKey", apiKeyResponse));
        } catch (Exception e) {
            log.error("Error creating project", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create project"));
        }
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> listProjects(Authentication authentication) {
        try {
            UUID userId = getCurrentUserId(authentication);
            List<Project> projects = projectService.listProjects(userId);
            List<ProjectResponse> responses = projects.stream()
                    .map(mapper::toResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("Error listing projects", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProject(
            @PathVariable UUID id,
            Authentication authentication) {
        try {
            UUID userId = getCurrentUserId(authentication);
            Project project = projectService.getProject(id, userId);
            return ResponseEntity.ok(mapper.toResponse(project));
        } catch (IllegalArgumentException e) {
            // Return 404 to prevent leaking project existence
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting project", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get project"));
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectRequest request,
            Authentication authentication) {
        try {
            UUID userId = getCurrentUserId(authentication);
            UpdateProjectCommand command = new UpdateProjectCommand(
                    id,
                    userId,
                    request.getName(),
                    request.getDescription());
            Project project = projectService.updateProject(command);
            return ResponseEntity.ok(mapper.toResponse(project));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error updating project", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update project"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(
            @PathVariable UUID id,
            Authentication authentication) {
        try {
            UUID userId = getCurrentUserId(authentication);
            projectService.deleteProject(id, userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error deleting project", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete project"));
        }
    }

    @PostMapping("/{id}/api-key/regenerate")
    public ResponseEntity<?> regenerateApiKey(
            @PathVariable UUID id,
            Authentication authentication) {
        try {
            UUID userId = getCurrentUserId(authentication);
            String newApiKey = projectService.regenerateApiKey(id, userId);
            ApiKeyResponse response = new ApiKeyResponse(
                    newApiKey,
                    mapper.maskApiKey(newApiKey));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error regenerating API key", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to regenerate API key"));
        }
    }

    @GetMapping("/{id}/api-key")
    public ResponseEntity<?> getApiKey(
            @PathVariable UUID id,
            Authentication authentication) {
        try {
            UUID userId = getCurrentUserId(authentication);
            Project project = projectService.getProject(id, userId);
            // Return masked key only
            ApiKeyResponse response = new ApiKeyResponse(
                    null,
                    mapper.maskApiKey(project.getApiKey()));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting API key", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get API key"));
        }
    }

    @GetMapping("/{id}/api-key/usage")
    public ResponseEntity<?> getApiKeyUsage(
            @PathVariable UUID id,
            Authentication authentication) {
        try {
            UUID userId = getCurrentUserId(authentication);
            // Validate project exists and belongs to user
            projectService.getProject(id, userId);

            // TODO: Implement actual usage tracking
            // For MVP, return placeholder data
            ApiKeyUsageResponse response = new ApiKeyUsageResponse(
                    0, // requestCount24h
                    0, // error4xxCount
                    0, // error5xxCount
                    1000 // rateLimitRemaining (default)
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting API key usage", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get API key usage"));
        }
    }
}

