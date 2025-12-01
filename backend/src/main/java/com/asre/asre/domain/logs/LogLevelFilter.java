package com.asre.asre.domain.logs;

import lombok.Value;

import java.util.Set;

/**
 * Value object representing a filter for log levels.
 * Supports single-level, ranges, and sets of levels.
 */
@Value
public class LogLevelFilter {
    Set<LogLevel> levels;

    public LogLevelFilter(Set<LogLevel> levels) {
        if (levels == null || levels.isEmpty()) {
            throw new IllegalArgumentException("Log level filter cannot be null or empty");
        }
        this.levels = Set.copyOf(levels);
    }

    /**
     * Create a filter for errors only (ERROR and FATAL).
     */
    public static LogLevelFilter errorsOnly() {
        return new LogLevelFilter(Set.of(LogLevel.ERROR, LogLevel.FATAL));
    }

    /**
     * Create a filter for a single level.
     */
    public static LogLevelFilter single(LogLevel level) {
        return new LogLevelFilter(Set.of(level));
    }

    /**
     * Create a filter for levels at or above the minimum.
     */
    public static LogLevelFilter atLeast(LogLevel minLevel) {
        Set<LogLevel> allLevels = Set.of(
                LogLevel.TRACE, LogLevel.DEBUG, LogLevel.INFO,
                LogLevel.WARN, LogLevel.ERROR, LogLevel.FATAL
        );
        Set<LogLevel> filtered = allLevels.stream()
                .filter(level -> level.ordinal() >= minLevel.ordinal())
                .collect(java.util.stream.Collectors.toSet());
        return new LogLevelFilter(filtered);
    }

    /**
     * Check if a level matches this filter.
     */
    public boolean matches(LogLevel level) {
        return levels.contains(level);
    }
}


