package com.jimm.serverdoctor;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Cleanup settings from {@code cleanup:} in config.yml.
 */
public final class CleanupConfig {

    private boolean enabled;
    private boolean includeDroppedItems;
    private boolean includeHostileMobs;
    private boolean includePassiveMobs;
    private int cooldownSeconds;
    private boolean logActions;

    public void load(FileConfiguration config) {
        enabled = config.getBoolean("cleanup.enabled", true);
        includeDroppedItems = config.getBoolean("cleanup.include-dropped-items", true);
        includeHostileMobs = config.getBoolean("cleanup.include-hostile-mobs", false);
        includePassiveMobs = config.getBoolean("cleanup.include-passive-mobs", false);
        cooldownSeconds = ConfigDefaults.atLeastZero(config.getInt("cleanup.cooldown-seconds", 300));
        logActions = config.getBoolean("cleanup.log-actions", true);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isIncludeDroppedItems() {
        return includeDroppedItems;
    }

    public boolean isIncludeHostileMobs() {
        return includeHostileMobs;
    }

    public boolean isIncludePassiveMobs() {
        return includePassiveMobs;
    }

    /**
     * How many entities would be removed by confirm (config flags + protection rules).
     */
    public int countEligibleToRemove() {
        return CleanupExecuteService.countEligibleToRemove(this);
    }

    public int getCooldownSeconds() {
        return cooldownSeconds;
    }

    public boolean isLogActions() {
        return logActions;
    }
}
