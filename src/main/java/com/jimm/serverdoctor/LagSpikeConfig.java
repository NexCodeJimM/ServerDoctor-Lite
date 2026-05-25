package com.jimm.serverdoctor;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Settings from {@code lag-spike-detection:} in config.yml.
 */
public final class LagSpikeConfig {

    private boolean enabled;
    private int checkIntervalSeconds;
    private double msptSpikeThreshold;
    private double tpsDropThreshold;
    private int alertCooldownSeconds;
    private boolean logSpikes;

    public void load(FileConfiguration config) {
        enabled = config.getBoolean("lag-spike-detection.enabled", true);
        checkIntervalSeconds = ConfigDefaults.atLeast(
                config.getInt("lag-spike-detection.check-interval-seconds", 30),
                1,
                30
        );
        msptSpikeThreshold = ConfigDefaults.clamp(
                config.getDouble("lag-spike-detection.mspt-spike-threshold", 80.0),
                0.0,
                1000.0,
                80.0
        );
        tpsDropThreshold = ConfigDefaults.clamp(
                config.getDouble("lag-spike-detection.tps-drop-threshold", 15.0),
                0.1,
                20.0,
                15.0
        );
        alertCooldownSeconds = ConfigDefaults.atLeastZero(config.getInt("lag-spike-detection.alert-cooldown-seconds", 300));
        logSpikes = config.getBoolean("lag-spike-detection.log-spikes", true);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getCheckIntervalSeconds() {
        return checkIntervalSeconds;
    }

    public double getMsptSpikeThreshold() {
        return msptSpikeThreshold;
    }

    public double getTpsDropThreshold() {
        return tpsDropThreshold;
    }

    public int getAlertCooldownSeconds() {
        return alertCooldownSeconds;
    }

    public boolean isLogSpikes() {
        return logSpikes;
    }
}
