package com.asre.asre.application.logs;

import com.asre.asre.domain.ingestion.LogEntry;
import com.asre.asre.domain.logs.LogSamplingPolicy;
import com.asre.asre.domain.logs.LogSamplingPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application service for log sampling.
 * Applies domain sampling policies to log entries.
 */
@Service
@RequiredArgsConstructor
public class LogSamplingService {

    private final LogSamplingPolicyRepository policyRepository;
    private final Random random = new Random();

    /**
     * Apply sampling policy to a list of log entries.
     * Returns filtered list with sampled flag set appropriately.
     */
    public List<LogEntry> applySampling(List<LogEntry> logEntries, UUID projectId) {
        if (logEntries.isEmpty()) {
            return logEntries;
        }

        // Get sampling policy for project (or default)
        LogSamplingPolicy policy = policyRepository.findByProjectId(projectId);

        return logEntries.stream()
                .map(logEntry -> {
                    double samplingRate = policy.getSamplingRate(
                            logEntry.getServiceId(),
                            logEntry.getLevel()
                    );
                    
                    boolean shouldKeep = shouldKeepLog(samplingRate);
                    logEntry.setSampled(!shouldKeep || samplingRate < 1.0);
                    
                    return shouldKeep ? logEntry : null;
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Determine if a log should be kept based on sampling rate.
     */
    private boolean shouldKeepLog(double samplingRate) {
        if (samplingRate >= 1.0) {
            return true; // 100% - keep all
        }
        if (samplingRate <= 0.0) {
            return false; // 0% - keep none
        }
        return random.nextDouble() < samplingRate;
    }
}


