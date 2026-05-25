package com.jimm.serverdoctor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;

/**
 * Appends cleanup confirm actions to {@code plugins/ServerDoctor/logs/cleanup.log}.
 */
public final class CleanupActionLogger {

    private static final String LOG_FILE_NAME = "cleanup.log";

    private CleanupActionLogger() {
    }

    public static void log(ServerDoctorPlugin plugin, CleanupExecuteResult result) {
        PluginDataFolders.ensureReady(plugin);
        File logFile = new File(new File(plugin.getDataFolder(), "logs"), LOG_FILE_NAME);
        String line = formatLogLine(result);

        try {
            Files.writeString(
                    logFile.toPath(),
                    line,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "Failed to write cleanup log", exception);
        }
    }

    private static String formatLogLine(CleanupExecuteResult result) {
        return String.format(
                "[%s] executor=%s worlds_scanned=%d dropped_items=%d hostile_mobs=%d passive_mobs=%d total=%d%n",
                result.formattedExecutedAt(),
                result.executedBy(),
                result.worldsScanned(),
                result.droppedItemsRemoved(),
                result.hostileMobsRemoved(),
                result.passiveMobsRemoved(),
                result.totalRemoved()
        );
    }
}
