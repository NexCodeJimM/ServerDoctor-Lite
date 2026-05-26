package com.jimm.serverdoctor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.logging.Level;

/**
 * Appends lightweight CSV lines under {@code plugins/ServerDoctor/history/}.
 */
public final class PerformanceHistoryFileLogger {

    private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String CSV_HEADER = "time,tps,mspt,memory_percent,loaded_chunks,entities";

    private PerformanceHistoryFileLogger() {
    }

    public static void append(ServerDoctorPlugin plugin, PerformanceSnapshot snapshot) {
        File historyFolder = new File(plugin.getDataFolder(), "history");
        if (!historyFolder.exists() && !historyFolder.mkdirs()) {
            plugin.getLogger().log(Level.WARNING, "Could not create history folder: {0}", historyFolder.getAbsolutePath());
            return;
        }

        String date = snapshot.recordedAt().format(FILE_DATE);
        File logFile = new File(historyFolder, "performance-" + date + ".csv");
        boolean writeHeader = !logFile.exists();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            if (writeHeader) {
                writer.write(CSV_HEADER);
                writer.newLine();
            }
            writer.write(String.format(
                    Locale.ROOT,
                    "%s,%.2f,%.2f,%.1f,%d,%d",
                    snapshot.formattedTime(),
                    snapshot.tps(),
                    snapshot.mspt(),
                    snapshot.memoryUsagePercent(),
                    snapshot.loadedChunkCount(),
                    snapshot.entityCount()
            ));
            writer.newLine();
        } catch (IOException exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to write performance history file", exception);
        }
    }
}
