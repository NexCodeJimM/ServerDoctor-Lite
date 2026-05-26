package com.jimm.serverdoctor;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Settings from {@code plugin-impact-scanner:} in config.yml.
 */
public final class PluginImpactScannerConfig {

    private boolean enabled;
    private boolean showPluginList;
    private int taskWarningThreshold;
    private int listenerWarningThreshold;

    public void load(FileConfiguration config) {
        enabled = config.getBoolean("plugin-impact-scanner.enabled", true);
        showPluginList = config.getBoolean("plugin-impact-scanner.show-plugin-list", true);
        taskWarningThreshold = ConfigDefaults.atLeast(
                config.getInt("plugin-impact-scanner.task-warning-threshold", 100),
                1,
                100
        );
        listenerWarningThreshold = ConfigDefaults.atLeast(
                config.getInt("plugin-impact-scanner.listener-warning-threshold", 100),
                1,
                100
        );
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isShowPluginList() {
        return showPluginList;
    }

    public int getTaskWarningThreshold() {
        return taskWarningThreshold;
    }

    public int getListenerWarningThreshold() {
        return listenerWarningThreshold;
    }
}
