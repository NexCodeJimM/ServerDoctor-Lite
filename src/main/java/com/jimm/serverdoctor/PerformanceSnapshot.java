package com.jimm.serverdoctor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Lightweight performance sample stored in memory (and optionally on disk).
 */
public record PerformanceSnapshot(
        LocalDateTime recordedAt,
        double tps,
        double mspt,
        double memoryUsagePercent,
        int loadedChunkCount,
        long entityCount
) {

    private static final DateTimeFormatter DISPLAY_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String formattedTime() {
        return recordedAt.format(DISPLAY_TIME);
    }
}
