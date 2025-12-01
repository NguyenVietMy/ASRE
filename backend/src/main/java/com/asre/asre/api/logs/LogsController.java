package com.asre.asre.api.logs;

import com.asre.asre.api.logs.dto.*;
import com.asre.asre.application.logs.LogQueryService;
import com.asre.asre.application.logs.LogVolumeService;
import com.asre.asre.domain.logs.*;
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
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@Slf4j
public class LogsController {

    private final LogQueryService logQueryService;
    private final LogVolumeService logVolumeService;
    private final com.asre.asre.application.logs.LogContextService logContextService;
    private final LogsDtoMapper mapper;

    @GetMapping("/query")
    public ResponseEntity<?> queryLogs(
            @Valid LogQueryRequest request,
            @RequestHeader("X-Project-ID") String projectIdHeader) {
        try {
            UUID projectId = UUID.fromString(projectIdHeader);
            LogQuery query = mapper.toDomainQuery(request, projectId);
            LogQueryResult result = logQueryService.queryLogs(query);
            return ResponseEntity.ok(mapper.toResponse(result));
        } catch (InvalidLogQueryException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error querying logs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to query logs"));
        }
    }

    @GetMapping("/{logId}/context")
    public ResponseEntity<?> getLogContext(
            @PathVariable String logId,
            @RequestHeader("X-Project-ID") String projectIdHeader,
            @RequestParam(defaultValue = "10") int before,
            @RequestParam(defaultValue = "10") int after) {
        try {
            UUID projectId = UUID.fromString(projectIdHeader);
            LogId domainLogId = new LogId(logId);
            com.asre.asre.domain.logs.LogContextService.LogContext context = 
                    logContextService.getContext(domainLogId, projectId, before, after);
            
            // Convert to response DTO
            LogQueryResponse.LogEntry targetLog = mapper.toLogEntryDto(context.targetLog());
            List<LogQueryResponse.LogEntry> beforeLogs = context.beforeLogs().stream()
                    .map(mapper::toLogEntryDto)
                    .collect(Collectors.toList());
            List<LogQueryResponse.LogEntry> afterLogs = context.afterLogs().stream()
                    .map(mapper::toLogEntryDto)
                    .collect(Collectors.toList());
            
            Map<String, Object> response = Map.of(
                    "targetLog", targetLog,
                    "beforeLogs", beforeLogs,
                    "afterLogs", afterLogs
            );
            
            return ResponseEntity.ok(response);
        } catch (LogNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error getting log context", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get log context"));
        }
    }

    @GetMapping("/volume")
    public ResponseEntity<?> queryVolume(
            @Valid LogVolumeRequest request,
            @RequestHeader("X-Project-ID") String projectIdHeader) {
        try {
            UUID projectId = UUID.fromString(projectIdHeader);
            LogVolumeQuery query = mapper.toDomainVolumeQuery(request, projectId);
            LogVolumeResult result = logVolumeService.queryVolume(query);
            return ResponseEntity.ok(mapper.toVolumeResponse(result));
        } catch (InvalidLogQueryException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error querying log volume", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to query log volume"));
        }
    }

    @GetMapping("/error-spikes")
    public ResponseEntity<?> queryErrorSpikes(
            @Valid ErrorSpikeRequest request,
            @RequestHeader("X-Project-ID") String projectIdHeader) {
        try {
            UUID projectId = UUID.fromString(projectIdHeader);
            ErrorSpikeQuery query = mapper.toDomainErrorSpikeQuery(request, projectId);
            ErrorSpikeResult result = logVolumeService.detectErrorSpikes(query);
            return ResponseEntity.ok(mapper.toErrorSpikeResponse(result));
        } catch (InvalidLogQueryException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error querying error spikes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to query error spikes"));
        }
    }

    @GetMapping("/trace/{traceId}")
    public ResponseEntity<?> queryTraceLogs(
            @PathVariable String traceId,
            @RequestHeader("X-Project-ID") String projectIdHeader) {
        try {
            UUID projectId = UUID.fromString(projectIdHeader);
            TraceLogRequest request = new TraceLogRequest(traceId);
            TraceLogQuery query = mapper.toDomainTraceQuery(request, projectId);
            TraceLogResult result = logQueryService.queryTraceLogs(query);
            return ResponseEntity.ok(mapper.toTraceResponse(result));
        } catch (InvalidLogQueryException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error querying trace logs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to query trace logs"));
        }
    }
}

