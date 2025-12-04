package com.asre.asre.api.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceResponse {
    private UUID id;
    private String name;
    private Instant firstSeen;
    private Instant lastSeen;
    private String status; // Healthy, Degraded, Down
    private int incidentCountRecent;
}


