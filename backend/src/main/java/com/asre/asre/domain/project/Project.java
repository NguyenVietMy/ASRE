package com.asre.asre.domain.project;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    private UUID id;
    private String name;
    private String apiKey;
    private UUID ownerUserId;
    private Integer rateLimitPerMinute;
    private Instant createdAt;

    public boolean isValidApiKey(String apiKey) {
        return this.apiKey != null && this.apiKey.equals(apiKey);
    }
}
