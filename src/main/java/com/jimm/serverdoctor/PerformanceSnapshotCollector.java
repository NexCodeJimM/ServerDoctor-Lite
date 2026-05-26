package com.jimm.serverdoctor;

import org.bukkit.Bukkit;
import org.bukkit.World;

import java.time.LocalDateTime;

/**
 * Collects a small performance snapshot without building a full {@link ServerStats} report.
 */
public final class PerformanceSnapshotCollector {

    private PerformanceSnapshotCollector() {
    }

    public static PerformanceSnapshot collect() {
        Runtime runtime = Runtime.getRuntime();
        long usedBytes = runtime.totalMemory() - runtime.freeMemory();
        long maxBytes = runtime.maxMemory();
        double memoryPercent = 0.0;
        if (maxBytes > 0) {
            memoryPercent = (usedBytes * 100.0) / maxBytes;
        }

        int loadedChunks = 0;
        long entityCount = 0;
        for (World world : Bukkit.getWorlds()) {
            loadedChunks += world.getLoadedChunks().length;
            entityCount += world.getEntities().size();
        }

        return new PerformanceSnapshot(
                LocalDateTime.now(),
                ServerPerformance.readCurrentTps(),
                ServerPerformance.readMspt(),
                memoryPercent,
                loadedChunks,
                entityCount
        );
    }
}
