package com.asre.asre.infra.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IngestionMetrics {

    public final MeterRegistry meterRegistry;

    // Metrics ingestion counters
    public Counter metricsIngested() {
        return Counter.builder("ingestion.metrics.count")
                .description("Total number of metrics ingested")
                .register(meterRegistry);
    }

    public Counter metricsIngestionErrors() {
        return Counter.builder("ingestion.metrics.errors")
                .description("Total number of metric ingestion errors")
                .register(meterRegistry);
    }

    public Timer metricsIngestionDuration() {
        return Timer.builder("ingestion.metrics.duration")
                .description("Time taken to ingest metrics batch")
                .register(meterRegistry);
    }

    // Logs ingestion counters
    public Counter logsIngested() {
        return Counter.builder("ingestion.logs.count")
                .description("Total number of logs ingested")
                .register(meterRegistry);
    }

    public Counter logsIngestionErrors() {
        return Counter.builder("ingestion.logs.errors")
                .description("Total number of log ingestion errors")
                .register(meterRegistry);
    }

    public Timer logsIngestionDuration() {
        return Timer.builder("ingestion.logs.duration")
                .description("Time taken to ingest logs batch")
                .register(meterRegistry);
    }

    // API gateway metrics
    public Counter apiRequests() {
        return Counter.builder("api.requests.total")
                .description("Total number of API requests")
                .register(meterRegistry);
    }

    public Counter rateLimitExceeded() {
        return Counter.builder("api.rate_limit.exceeded")
                .description("Total number of rate limit exceeded requests")
                .register(meterRegistry);
    }

    public Counter apiKeyInvalid() {
        return Counter.builder("api.apikey.invalid")
                .description("Total number of invalid API key requests")
                .register(meterRegistry);
    }
}

