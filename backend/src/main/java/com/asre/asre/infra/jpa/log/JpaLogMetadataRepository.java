package com.asre.asre.infra.jpa.log;

import com.asre.asre.domain.ingestion.LogMetadata;
import com.asre.asre.domain.ingestion.LogMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class JpaLogMetadataRepository implements LogMetadataRepository {

    private static final String INSERT_SQL = """
            INSERT INTO log_metadata (project_id, service_id, log_id, timestamp, ingested_at, level)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    @Qualifier("supabaseJdbcTemplate")
    private final JdbcTemplate supabaseJdbcTemplate;

    @Override
    public void saveBatch(List<LogMetadata> logMetadataList) {
        if (logMetadataList.isEmpty()) {
            return;
        }

        List<Object[]> batchArgs = logMetadataList.stream()
                .map(metadata -> new Object[] {
                        metadata.getProjectId(),
                        metadata.getServiceId(),
                        metadata.getLogId(),
                        Timestamp.from(metadata.getTimestamp()),
                        Timestamp.from(metadata.getIngestedAt()),
                        metadata.getLevel()
                })
                .collect(Collectors.toList());

        try {
            int[] results = supabaseJdbcTemplate.batchUpdate(INSERT_SQL, batchArgs);
            log.debug("Inserted {} log metadata records into PostgreSQL", results.length);
        } catch (Exception e) {
            log.error("Error inserting log metadata batch", e);
            throw new RuntimeException("Failed to insert log metadata", e);
        }
    }
}
