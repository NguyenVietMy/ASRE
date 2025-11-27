package com.asre.asre.infra.metrics;

import com.asre.asre.domain.ingestion.MetricsCollector;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MetricsCollectorImpl implements MetricsCollector {

    private final MeterRegistry meterRegistry;

    @Override
    public void recordMetricsIngested(long count) {
        Counter.builder("ingestion.metrics.count")
                .description("Total number of metrics ingested")
                .register(meterRegistry)
                .increment(count);
    }

    @Override
    public void recordMetricsIngestionError() {
        Counter.builder("ingestion.metrics.errors")
                .description("Total number of metric ingestion errors")
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordMetricsIngestionDuration(long durationMs) {
        Timer.builder("ingestion.metrics.duration")
                .description("Time taken to ingest metrics batch")
                .register(meterRegistry)
                .record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    @Override
    public void recordLogsIngested(long count) {
        Counter.builder("ingestion.logs.count")
                .description("Total number of logs ingested")
                .register(meterRegistry)
                .increment(count);
    }

    @Override
    public void recordLogsIngestionError() {
        Counter.builder("ingestion.logs.errors")
                .description("Total number of log ingestion errors")
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordLogsIngestionDuration(long durationMs) {
        Timer.builder("ingestion.logs.duration")
                .description("Time taken to ingest logs batch")
                .register(meterRegistry)
                .record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    }
}


