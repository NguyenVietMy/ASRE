package com.asre.asre.api.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDetailResponse {
    private UUID id;
    private String name;
    private UUID projectId;
    private Instant firstSeen;
    private Instant lastSeen;
    private int activeIncidents;
}


