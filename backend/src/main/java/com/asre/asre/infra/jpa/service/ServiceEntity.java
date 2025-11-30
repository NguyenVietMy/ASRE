package com.asre.asre.infra.jpa.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity representation for Service - persistence layer only.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceEntity {
    private UUID id;
    private UUID projectId;
    private String name;
    private Instant createdAt;
    private Instant lastSeenAt;
}

