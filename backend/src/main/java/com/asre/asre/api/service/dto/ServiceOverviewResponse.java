package com.asre.asre.api.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceOverviewResponse {
    private UUID serviceId;
    private String serviceName;
    private Double errorRate;
    private Double p95Latency;
    private Double throughput;
    private int openIncidents;
}


