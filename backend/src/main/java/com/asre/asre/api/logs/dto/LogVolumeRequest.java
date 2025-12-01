package com.asre.asre.api.logs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogVolumeRequest {
    @NotNull
    private String startTime;

    @NotNull
    private String endTime;

    @NotBlank
    private String rollup; // 1m, 5m, 1h, 1d

    private String level; // Optional level filter

    private UUID serviceId;
}


