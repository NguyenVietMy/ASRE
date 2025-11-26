package com.asre.asre.domain.ingestion;

import java.util.List;

public interface MetricRepository {
    void saveBatch(List<Metric> metrics);
}
