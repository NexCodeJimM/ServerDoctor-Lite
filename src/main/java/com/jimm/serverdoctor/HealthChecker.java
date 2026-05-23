package com.jimm.serverdoctor;

import java.util.ArrayList;
import java.util.List;

public final class HealthChecker {

    private HealthChecker() {
    }

    public static List<AlertType> findActiveAlerts(ServerStats stats, PluginConfig config) {
        List<AlertType> active = new ArrayList<>();

        if (stats.memoryUsagePercent > config.getMemoryWarningPercent()) {
            active.add(AlertType.MEMORY);
        }
        if (stats.entityCount > config.getEntityWarningLimit()) {
            active.add(AlertType.ENTITIES);
        }
        if (stats.loadedChunkCount > config.getLoadedChunkWarningLimit()) {
            active.add(AlertType.CHUNKS);
        }
        if (stats.currentTps < config.getTpsWarningThreshold()) {
            active.add(AlertType.TPS);
        }
        if (stats.mspt > config.getMsptWarningThreshold()) {
            active.add(AlertType.MSPT);
        }

        return active;
    }

    public static String formatReportWarning(AlertType type, ServerStats stats, PluginConfig config) {
        return switch (type) {
            case MEMORY -> String.format(
                    "Memory usage is above %.0f%% (currently %.1f%%)",
                    config.getMemoryWarningPercent(),
                    stats.memoryUsagePercent
            );
            case ENTITIES -> String.format(
                    "Entity count is above %,d (currently %,d)",
                    config.getEntityWarningLimit(),
                    stats.entityCount
            );
            case CHUNKS -> String.format(
                    "Loaded chunk count is above %,d (currently %,d)",
                    config.getLoadedChunkWarningLimit(),
                    stats.loadedChunkCount
            );
            case TPS -> String.format(
                    "TPS is below %.1f (currently %.2f)",
                    config.getTpsWarningThreshold(),
                    stats.currentTps
            );
            case MSPT -> String.format(
                    "MSPT is above %.1f ms (currently %.2f ms)",
                    config.getMsptWarningThreshold(),
                    stats.mspt
            );
        };
    }

    public static String alertTypeName(AlertType type) {
        return switch (type) {
            case MEMORY -> "Memory";
            case ENTITIES -> "Entities";
            case CHUNKS -> "Loaded Chunks";
            case TPS -> "TPS";
            case MSPT -> "MSPT";
        };
    }

    public static String formatCurrentValue(AlertType type, ServerStats stats) {
        return switch (type) {
            case MEMORY -> String.format("%.1f%%", stats.memoryUsagePercent);
            case ENTITIES -> String.format("%,d", stats.entityCount);
            case CHUNKS -> String.format("%,d", stats.loadedChunkCount);
            case TPS -> String.format("%.2f", stats.currentTps);
            case MSPT -> String.format("%.2f ms", stats.mspt);
        };
    }

    public static String formatThresholdValue(AlertType type, PluginConfig config) {
        return switch (type) {
            case MEMORY -> String.format("%.0f%%", config.getMemoryWarningPercent());
            case ENTITIES -> String.format("%,d", config.getEntityWarningLimit());
            case CHUNKS -> String.format("%,d", config.getLoadedChunkWarningLimit());
            case TPS -> String.format("%.1f", config.getTpsWarningThreshold());
            case MSPT -> String.format("%.1f ms", config.getMsptWarningThreshold());
        };
    }

    public static String formatBroadcastAlert(AlertType type, ServerStats stats, PluginConfig config) {
        return switch (type) {
            case MEMORY -> String.format(
                    "High memory: %.1f%% (limit %.0f%%)",
                    stats.memoryUsagePercent,
                    config.getMemoryWarningPercent()
            );
            case ENTITIES -> String.format(
                    "High entity count: %,d (limit %,d)",
                    stats.entityCount,
                    config.getEntityWarningLimit()
            );
            case CHUNKS -> String.format(
                    "High loaded chunks: %,d (limit %,d)",
                    stats.loadedChunkCount,
                    config.getLoadedChunkWarningLimit()
            );
            case TPS -> String.format(
                    "Low TPS: %.2f (limit %.1f)",
                    stats.currentTps,
                    config.getTpsWarningThreshold()
            );
            case MSPT -> String.format(
                    "High MSPT: %.2f ms (limit %.1f ms)",
                    stats.mspt,
                    config.getMsptWarningThreshold()
            );
        };
    }
}
