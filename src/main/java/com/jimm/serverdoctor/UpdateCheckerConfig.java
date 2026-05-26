package com.jimm.serverdoctor;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Settings from {@code update-checker:} in config.yml.
 */
public final class UpdateCheckerConfig {

    public static final String SPIGOT_RESOURCE_URL =
            "https://www.spigotmc.org/resources/serverdoctor.135602/";
    public static final int RESOURCE_ID = 135602;

    private boolean enabled;
    private boolean checkOnStartup;
    private boolean notifyOpsOnJoin;

    public void load(FileConfiguration config) {
        enabled = config.getBoolean("update-checker.enabled", true);
        checkOnStartup = config.getBoolean("update-checker.check-on-startup", true);
        notifyOpsOnJoin = config.getBoolean("update-checker.notify-ops-on-join", true);
    }

    public boolean isEnabled() {
        return enabled;
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
        return "https://api.spigotmc.org/legacy/update.php?resource=" + RESOURCE_ID;
    }
}
