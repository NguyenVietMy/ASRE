package com.asre.asre.api.logs;

import com.asre.asre.api.logs.dto.*;
import com.asre.asre.domain.ingestion.LogEntry;
import com.asre.asre.domain.logs.*;
import com.asre.asre.domain.metrics.RollupPeriod;
import com.asre.asre.domain.metrics.TimeRange;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper between API DTOs and domain objects for logs.
 */
@Component
public class LogsDtoMapper {

    public LogQuery toDomainQuery(LogQueryRequest request, UUID projectId) {
        // Parse time range
        TimeRange timeRange = new TimeRange(
                Instant.parse(request.getStartTime()),
                Instant.parse(request.getEndTime())
        );

        // Parse service filter
        ServiceFilter serviceFilter = request.getServiceId() != null
                ? ServiceFilter.forService(request.getServiceId())
                : null;

        // Parse level filter
        LogLevelFilter levelFilter = null;
        if (request.getLevel() != null && !request.getLevel().isBlank()) {
            List<String> levels = Arrays.asList(request.getLevel().split(","));
            Set<LogLevel> levelSet = levels.stream()
                    .map(String::trim)
                    .map(LogLevel::fromString)
                    .collect(Collectors.toSet());
            levelFilter = new LogLevelFilter(levelSet);
        }

        // Parse sort order
        LogSortOrder.LogSortField sortField = "ingested_at".equalsIgnoreCase(request.getSortField())
                ? LogSortOrder.LogSortField.INGESTED_AT
                : LogSortOrder.LogSortField.TIMESTAMP;
        LogSortOrder.SortDirection direction = "asc".equalsIgnoreCase(request.getSort())
                ? LogSortOrder.SortDirection.ASC
                : LogSortOrder.SortDirection.DESC;
        LogSortOrder sortOrder = new LogSortOrder(sortField, direction);

        // Parse pagination token
        LogPaginationToken paginationToken = request.getSearchAfter() != null
                ? LogPaginationToken.fromString(request.getSearchAfter())
                : null;

        return new LogQuery(
                projectId,
                timeRange,
                serviceFilter,
                levelFilter,
                request.getSearch(),
                request.getTraceId(),
                sortOrder,
                request.getLimit(),
                paginationToken
        );
    }

    public LogVolumeQuery toDomainVolumeQuery(LogVolumeRequest request, UUID projectId) {
        TimeRange timeRange = new TimeRange(
                Instant.parse(request.getStartTime()),
                Instant.parse(request.getEndTime())
        );

        RollupPeriod rollupPeriod = new RollupPeriod(request.getRollup());

        ServiceFilter serviceFilter = request.getServiceId() != null
                ? ServiceFilter.forService(request.getServiceId())
                : null;

        LogLevelFilter levelFilter = null;
        if (request.getLevel() != null && !request.getLevel().isBlank()) {
            levelFilter = LogLevelFilter.single(LogLevel.fromString(request.getLevel()));
        }

        return new LogVolumeQuery(
                projectId,
                timeRange,
                rollupPeriod,
                serviceFilter,
                levelFilter
        );
    }

    public ErrorSpikeQuery toDomainErrorSpikeQuery(ErrorSpikeRequest request, UUID projectId) {
        TimeRange timeRange = new TimeRange(
                Instant.parse(request.getStartTime()),
                Instant.parse(request.getEndTime())
        );

        RollupPeriod rollupPeriod = new RollupPeriod(request.getRollup());

        ServiceFilter serviceFilter = request.getServiceId() != null
                ? ServiceFilter.forService(request.getServiceId())
                : null;

        return new ErrorSpikeQuery(
                projectId,
                timeRange,
                rollupPeriod,
                serviceFilter,
                request.getSpikeThreshold()
        );
    }

    public TraceLogQuery toDomainTraceQuery(TraceLogRequest request, UUID projectId) {
        return new TraceLogQuery(projectId, request.getTraceId());
    }

    public LogQueryResponse toResponse(LogQueryResult result) {
        List<LogQueryResponse.LogEntry> logEntries = result.getLogs().stream()
                .map(this::toLogEntryDto)
                .collect(Collectors.toList());

        String nextToken = result.getNextPageToken()
                .map(LogPaginationToken::getValue)
                .orElse(null);

        return new LogQueryResponse(logEntries, nextToken, result.getTotalReturned());
    }

    public LogVolumeResponse toVolumeResponse(LogVolumeResult result) {
        List<LogVolumeResponse.DataPoint> dataPoints = result.getDataPoints().stream()
                .map(point -> {
                    Long estimatedCount = point.getEstimatedCount()
                            .orElse(null);
                    return new LogVolumeResponse.DataPoint(
                            point.getTimestamp().toString(),
                            point.getCount(),
                            estimatedCount
                    );
                })
                .collect(Collectors.toList());

        return new LogVolumeResponse(dataPoints, result.isSampled());
    }

    public ErrorSpikeResponse toErrorSpikeResponse(ErrorSpikeResult result) {
        List<ErrorSpikeResponse.Spike> spikes = result.getSpikes().stream()
                .map(spike -> new ErrorSpikeResponse.Spike(
                        spike.getTimestamp().toString(),
                        spike.getErrorCount(),
                        spike.getSpikeSeverity(),
                        spike.getDescription().orElse(null)
                ))
                .collect(Collectors.toList());

        List<ErrorSpikeResponse.DataPoint> dataPoints = result.getAllDataPoints().stream()
                .map(point -> new ErrorSpikeResponse.DataPoint(
                        point.getTimestamp().toString(),
                        point.getCount()
                ))
                .collect(Collectors.toList());

        return new ErrorSpikeResponse(spikes, dataPoints);
    }

    public TraceLogResponse toTraceResponse(TraceLogResult result) {
        List<LogQueryResponse.LogEntry> logEntries = result.getLogsInOrder().stream()
                .map(this::toLogEntryDto)
                .collect(Collectors.toList());

        return new TraceLogResponse(result.getTraceId(), logEntries);
    }

    public LogQueryResponse.LogEntry toLogEntryDto(LogEntry logEntry) {
        return new LogQueryResponse.LogEntry(
                logEntry.getLogId().getValue(),
                logEntry.getProjectId().toString(),
                logEntry.getServiceId().toString(),
                logEntry.getLevel().name(),
                logEntry.getMessage(),
                logEntry.getTimestamp().toString(),
                logEntry.getTraceId(),
                logEntry.getContext(),
                logEntry.isSampled()
        );
    }
}

