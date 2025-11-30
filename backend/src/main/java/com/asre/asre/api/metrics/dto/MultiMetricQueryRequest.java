package com.asre.asre.api.metrics.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultiMetricQueryRequest {
    @NotEmpty
    @Valid
    private List<SingleQuery> queries;

    @NotNull
    private String startTime;

    @NotNull
    private String endTime;

    @NotBlank
    private String rollup;

    private boolean align = true;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SingleQuery {
        @NotBlank
        private String metric;

        @NotBlank
        private String stat;

        @NotNull
        private java.util.UUID serviceId;
    }
}

