package com.jimm.serverdoctor;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Settings from {@code update-checker:} in config.yml.
 */
public final class UpdateCheckerConfig {

    public static final String SPIGOT_RESOURCE_URL =
            "https://www.spigotmc.org/resources/serverdoctor.135585/";
    public static final int DEFAULT_RESOURCE_ID = 135585;

    private boolean enabled;
    private int spigotResourceId;
    private boolean checkOnStartup;
    private boolean notifyOpsOnJoin;

    public void load(FileConfiguration config) {
        enabled = config.getBoolean("update-checker.enabled", true);
        spigotResourceId = ConfigDefaults.atLeastZero(config.getInt("update-checker.spigot-resource-id", 0));
        checkOnStartup = config.getBoolean("update-checker.check-on-startup", true);
        notifyOpsOnJoin = config.getBoolean("update-checker.notify-ops-on-join", true);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getSpigotResourceId() {
        return spigotResourceId;
    }

    public boolean isConfigured() {
        return spigotResourceId > 0;
    }

    public boolean isCheckOnStartup() {
        return checkOnStartup;
    }

    public boolean isNotifyOpsOnJoin() {
        return notifyOpsOnJoin;
    }

    public String resourcePageUrl() {
        return SPIGOT_RESOURCE_URL;
    }

    public String updateApiUrl() {
        return "https://api.spigotmc.org/legacy/update.php?resource=" + spigotResourceId;
    }
}
