package com.jimm.serverdoctor;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Settings from {@code investigation:} in config.yml.
 */
public final class InvestigationConfig {

    private boolean enabled;
    private int autoStopMinutes;
    private boolean includeHeavyChunks;
    private boolean includeCleanupEvents;
    private boolean includeRecommendations;

    public void load(FileConfiguration config) {
        enabled = config.getBoolean("investigation.enabled", true);
        autoStopMinutes = ConfigDefaults.atLeast(config.getInt("investigation.auto-stop-minutes", 30), 1, 30);
        includeHeavyChunks = config.getBoolean("investigation.include-heavy-chunks", true);
        includeCleanupEvents = config.getBoolean("investigation.include-cleanup-events", true);
        includeRecommendations = config.getBoolean("investigation.include-recommendations", true);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getAutoStopMinutes() {
        return autoStopMinutes;
    }

    public boolean isIncludeHeavyChunks() {
        return includeHeavyChunks;
    }

    public boolean isIncludeCleanupEvents() {
        return includeCleanupEvents;
    }

    public boolean isIncludeRecommendations() {
        return includeRecommendations;
    }
}
