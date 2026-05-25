package com.jimm.serverdoctor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Snapshot captured when a lag spike is detected.
 */
public record LagSpikeEvent(
        LocalDateTime detectedAt,
        double tps,
        double mspt,
        double memoryUsagePercent,
        long entityCount,
        int loadedChunkCount,
        boolean tpsTriggered,
        boolean msptTriggered
) {

    private static final DateTimeFormatter DISPLAY_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String formattedDetectedAt() {
        return detectedAt.format(DISPLAY_TIME);
    }

    public String triggerSummary() {
        if (tpsTriggered && msptTriggered) {
            return "TPS and MSPT";
        }
        if (tpsTriggered) {
            return "TPS";
        }
        return "MSPT";
    }
}
