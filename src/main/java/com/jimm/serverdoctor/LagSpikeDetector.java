package com.jimm.serverdoctor;

import java.time.LocalDateTime;

/**
 * Detects sudden TPS drops and MSPT spikes from server stats.
 */
public final class LagSpikeDetector {

    private LagSpikeDetector() {
    }

    public static LagSpikeEvent detect(ServerStats stats, LagSpikeConfig config) {
        boolean tpsTriggered = stats.currentTps < config.getTpsDropThreshold();
        boolean msptTriggered = stats.mspt > config.getMsptSpikeThreshold();

        if (!tpsTriggered && !msptTriggered) {
            return null;
        }

        return new LagSpikeEvent(
                LocalDateTime.now(),
                stats.currentTps,
                stats.mspt,
                stats.memoryUsagePercent,
                stats.entityCount,
                stats.loadedChunkCount,
                tpsTriggered,
                msptTriggered
        );
    }
}
