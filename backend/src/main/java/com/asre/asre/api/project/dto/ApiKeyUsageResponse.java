package com.asre.asre.api.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyUsageResponse {
    private long requestCount24h;
    private long error4xxCount;
    private long error5xxCount;
    private int rateLimitRemaining;
}

