package com.asre.asre.api.ingestion;

import com.asre.asre.api.ingestion.dto.LogsIngestionRequest;
import com.asre.asre.api.ingestion.dto.MetricsIngestionRequest;
import com.asre.asre.application.ingestion.IngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/ingest")
@RequiredArgsConstructor
@Slf4j
public class IngestionController {

    private final IngestionService ingestionService;
    private final IngestionDtoMapper mapper;

    @PostMapping("/metrics")
    public ResponseEntity<?> ingestMetrics(
            @RequestBody MetricsIngestionRequest request,
            Authentication authentication) {
        try {
            // Extract project_id from authentication (set by ApiKeyAuthenticationFilter)
            UUID projectId = UUID.fromString(authentication.getName());

            // Convert DTO to domain command
            var command = mapper.toCommand(request, projectId);

            // Delegate to application service
            ingestionService.ingestMetrics(command);

            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            log.error("Error ingesting metrics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to ingest metrics"));
        }
    }

    @PostMapping("/logs")
    public ResponseEntity<?> ingestLogs(
            @RequestBody LogsIngestionRequest request,
            Authentication authentication) {
        try {
            // Extract project_id from authentication
            UUID projectId = UUID.fromString(authentication.getName());

            // Convert DTO to domain command
            var command = mapper.toCommand(request, projectId);

            // Delegate to application service
            ingestionService.ingestLogs(command);

            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            log.error("Error ingesting logs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to ingest logs"));
        }
    }
}
