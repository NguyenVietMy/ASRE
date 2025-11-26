package com.asre.asre.domain.ingestion;

import java.util.List;

public interface LogMetadataRepository {
    void saveBatch(List<LogMetadata> logMetadataList);
}
