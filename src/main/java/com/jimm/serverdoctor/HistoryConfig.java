package com.jimm.serverdoctor;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Settings from {@code history:} in config.yml.
 */
public final class HistoryConfig {

    private boolean enabled;
    private int sampleIntervalSeconds;
    private int maxHistoryEntries;
    private boolean saveHistoryFiles;

    public void load(FileConfiguration config) {
        enabled = config.getBoolean("history.enabled", true);
        sampleIntervalSeconds = ConfigDefaults.atLeast(
                config.getInt("history.sample-interval-seconds", 60),
                1,
                60
        );
        maxHistoryEntries = ConfigDefaults.atLeast(
                config.getInt("history.max-history-entries", 1440),
                10,
                1440
        );
        saveHistoryFiles = config.getBoolean("history.save-history-files", true);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getSampleIntervalSeconds() {
        return sampleIntervalSeconds;
    }

    public int getMaxHistoryEntries() {
        return maxHistoryEntries;
    }

    public boolean isSaveHistoryFiles() {
        return saveHistoryFiles;
    }
}
