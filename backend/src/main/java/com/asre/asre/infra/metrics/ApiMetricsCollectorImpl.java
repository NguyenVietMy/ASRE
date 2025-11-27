package com.asre.asre.infra.metrics;

import com.asre.asre.domain.ingestion.ApiMetricsCollector;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApiMetricsCollectorImpl implements ApiMetricsCollector {

    private final MeterRegistry meterRegistry;

    @Override
    public void recordApiRequest() {
        Counter.builder("api.requests.total")
                .description("Total number of API requests")
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordRateLimitExceeded() {
        Counter.builder("api.rate_limit.exceeded")
                .description("Total number of rate limit exceeded requests")
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordInvalidApiKey() {
        Counter.builder("api.apikey.invalid")
                .description("Total number of invalid API key requests")
                .register(meterRegistry)
                .increment();
    }
}


