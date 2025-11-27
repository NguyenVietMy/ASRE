package com.asre.asre.api.ingestion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogsIngestionRequest {
    @NotEmpty
    @Valid
    private List<LogDto> logs;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogDto {
        private String level;
        private String message;
        private UUID serviceId;
        private String timestamp;
        private String traceId;
        private Map<String, Object> context;
    }
}


