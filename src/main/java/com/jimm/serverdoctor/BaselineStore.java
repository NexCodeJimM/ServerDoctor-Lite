package com.jimm.serverdoctor;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Loads and saves the default baseline file on disk.
 */
final class BaselineStore {

    static final String DEFAULT_FILE_NAME = "default.yml";

    private final ServerDoctorPlugin plugin;
    private final File baselineFile;

    BaselineStore(ServerDoctorPlugin plugin) {
        this.plugin = plugin;
        this.baselineFile = new File(new File(plugin.getDataFolder(), "baselines"), DEFAULT_FILE_NAME);
    }

    File getBaselineFile() {
        return baselineFile;
    }

    boolean exists() {
        return baselineFile.isFile();
    }

    PerformanceBaseline load() {
        if (!exists()) {
            return null;
        }
        try {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(baselineFile);
            return PerformanceBaseline.fromConfig(yaml);
        } catch (Exception exception) {
            plugin.getLogger().log(
                    Level.WARNING,
                    "Could not read baseline file: {0}",
                    exception.getMessage()
            );
            return null;
        }
    }

    void save(PerformanceBaseline baseline) throws IOException {
        PluginDataFolders.ensureReady(plugin);
        baseline.save(baselineFile);
    }

    boolean delete() {
        if (!exists()) {
            return false;
        }
        if (baselineFile.delete()) {
            return true;
        }
        plugin.getLogger().warning("Could not delete baseline file: " + baselineFile.getAbsolutePath());
        return false;
    }
}
