package com.asre.asre.api.logs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogVolumeResponse {
    private List<DataPoint> data;
    private boolean isSampled;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataPoint {
        private String timestamp;
        private Long count;
        private Long estimatedCount; // If sampling was applied
    }
}


