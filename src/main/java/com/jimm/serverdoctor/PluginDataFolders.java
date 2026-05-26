package com.jimm.serverdoctor;

import java.io.File;
import java.util.logging.Level;

/**
 * Ensures plugin data, report, and log directories exist before writing files.
 */
public final class PluginDataFolders {

    private PluginDataFolders() {
    }

    public static void ensureReady(ServerDoctorPlugin plugin) {
        ensureDirectory(plugin, plugin.getDataFolder());
        ensureDirectory(plugin, new File(plugin.getDataFolder(), "reports"));
        ensureDirectory(plugin, new File(plugin.getDataFolder(), "logs"));
        ensureDirectory(plugin, new File(plugin.getDataFolder(), "history"));
        ensureDirectory(plugin, new File(plugin.getDataFolder(), "scheduled-reports"));
    }

    private static void ensureDirectory(ServerDoctorPlugin plugin, File directory) {
        if (directory.exists()) {
            return;
        }
        if (!directory.mkdirs()) {
            plugin.getLogger().log(
                    Level.WARNING,
                    "Could not create folder: {0}",
                    directory.getAbsolutePath()
            );
        }
    }
}
