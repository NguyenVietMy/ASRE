package com.asre.asre.domain.ingestion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogMetadata {
    private UUID projectId;
    private UUID serviceId;
    private String logId;
    private Instant timestamp;
    private Instant ingestedAt;
    private String level;
}
