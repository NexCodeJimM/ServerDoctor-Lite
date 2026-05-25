package com.jimm.serverdoctor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.logging.Level;

/**
 * Appends lag spike detections to {@code plugins/ServerDoctor/logs/lag-spikes.log}.
 */
public final class LagSpikeLogger {

    private static final String LOG_FILE_NAME = "lag-spikes.log";

    private LagSpikeLogger() {
    }

    public static void log(ServerDoctorPlugin plugin, LagSpikeEvent event) {
        PluginDataFolders.ensureReady(plugin);
        File logFile = new File(new File(plugin.getDataFolder(), "logs"), LOG_FILE_NAME);
        String line = formatLogLine(event);

        try {
            Files.writeString(
                    logFile.toPath(),
                    line,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "Failed to write lag spike log", exception);
        }
    }

    private static String formatLogLine(LagSpikeEvent event) {
        return String.format(
                Locale.ROOT,
                "[%s] tps=%.2f mspt=%.2f memory=%.1f%% entities=%d loaded_chunks=%d triggers=%s%n",
                event.formattedDetectedAt(),
                event.tps(),
                event.mspt(),
                event.memoryUsagePercent(),
                event.entityCount(),
                event.loadedChunkCount(),
                event.triggerSummary()
        );
    }
}
