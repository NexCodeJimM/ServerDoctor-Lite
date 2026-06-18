package com.jimm.serverdoctor;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Settings from {@code baseline:} in config.yml.
 */
public final class BaselineConfig {

    private boolean enabled;
    private double significantTpsDrop;
    private double significantMsptIncrease;
    private double significantMemoryIncreasePercent;
    private long significantEntityIncrease;
    private int significantChunkIncrease;

    public void load(FileConfiguration config) {
        enabled = config.getBoolean("baseline.enabled", true);
        significantTpsDrop = ConfigDefaults.clamp(
                config.getDouble("baseline.significant-tps-drop", 1.0), 0.1, 20.0, 1.0
        );
        significantMsptIncrease = ConfigDefaults.clamp(
                config.getDouble("baseline.significant-mspt-increase", 10.0), 0.1, 1000.0, 10.0
        );
        significantMemoryIncreasePercent = ConfigDefaults.clamp(
                config.getDouble("baseline.significant-memory-increase-percent", 15.0), 1.0, 100.0, 15.0
        );
        significantEntityIncrease = ConfigDefaults.atLeast(
                config.getLong("baseline.significant-entity-increase", 500L), 1, 500L
        );
        significantChunkIncrease = ConfigDefaults.atLeast(
                config.getInt("baseline.significant-chunk-increase", 1000), 1, 1000
        );
    }

    public boolean isEnabled() {
        return enabled;
    }

    public double getSignificantTpsDrop() {
        return significantTpsDrop;
    }

    public double getSignificantMsptIncrease() {
        return significantMsptIncrease;
    }

    public double getSignificantMemoryIncreasePercent() {
        return significantMemoryIncreasePercent;
    }

    public long getSignificantEntityIncrease() {
        return significantEntityIncrease;
    }

    public int getSignificantChunkIncrease() {
        return significantChunkIncrease;
    }
}
