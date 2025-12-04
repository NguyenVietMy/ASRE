package com.asre.asre.infra.jpa.project;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity representation for Project - persistence layer only.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectEntity {
    private UUID id;
    private String name;
    private String description;
    private String apiKey;
    private UUID ownerUserId;
    private Integer rateLimitPerMinute;
    private Instant createdAt;
    private Instant deletedAt;
}
