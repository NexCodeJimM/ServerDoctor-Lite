package com.jimm.serverdoctor;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Settings from {@code scheduled-reports:} in config.yml.
 */
public final class ScheduledReportsConfig {

    private boolean enabled;
    private int intervalHours;
    private boolean saveToFile;
    private boolean sendToDiscord;

    public void load(FileConfiguration config) {
        enabled = config.getBoolean("scheduled-reports.enabled", false);
        intervalHours = ConfigDefaults.atLeast(
                config.getInt("scheduled-reports.interval-hours", 24),
                1,
                24
        );
        saveToFile = config.getBoolean("scheduled-reports.save-to-file", true);
        sendToDiscord = config.getBoolean("scheduled-reports.send-to-discord", false);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getIntervalHours() {
        return intervalHours;
    }

    public boolean isSaveToFile() {
        return saveToFile;
    }

    public boolean isSendToDiscord() {
        return sendToDiscord;
    }

    public long intervalMillis() {
        return intervalHours * 3_600_000L;
    }
}
