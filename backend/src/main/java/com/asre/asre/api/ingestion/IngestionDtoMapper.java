package com.asre.asre.api.ingestion;

import com.asre.asre.api.ingestion.dto.LogsIngestionRequest;
import com.asre.asre.api.ingestion.dto.MetricsIngestionRequest;
import com.asre.asre.application.ingestion.IngestLogsCommand;
import com.asre.asre.application.ingestion.IngestMetricsCommand;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class IngestionDtoMapper {

        public IngestMetricsCommand toCommand(MetricsIngestionRequest request, UUID projectId) {
                List<IngestMetricsCommand.MetricData> metrics = request.getMetrics().stream()
                                .map(dto -> new IngestMetricsCommand.MetricData(
                                                dto.getServiceId(),
                                                dto.getName(),
                                                dto.getValue(),
                                                dto.getTimestamp(),
                                                dto.getTags()))
                                .collect(Collectors.toList());

                return new IngestMetricsCommand(projectId, metrics);
        }

        public IngestLogsCommand toCommand(LogsIngestionRequest request, UUID projectId) {
                List<IngestLogsCommand.LogData> logs = request.getLogs().stream()
                                .map(dto -> new IngestLogsCommand.LogData(
                                                dto.getServiceId(),
                                                dto.getLevel(),
                                                dto.getMessage(),
                                                dto.getTimestamp(),
                                                dto.getTraceId(),
                                                dto.getContext()))
                                .collect(Collectors.toList());

                return new IngestLogsCommand(projectId, logs);
        }
}
