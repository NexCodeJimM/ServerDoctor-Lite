package com.jimm.serverdoctor;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Snapshot saved as {@code plugins/ServerDoctor/baselines/default.yml}.
 */
public final class PerformanceBaseline {

    private static final DateTimeFormatter STORAGE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String createdAt;
    private final String createdBy;
    private final String serverVersion;
    private final String javaVersion;
    private final String pluginVersion;
    private final double tps;
    private final double mspt;
    private final double memoryUsagePercent;
    private final int onlinePlayers;
    private final String loadedWorlds;
    private final int loadedChunks;
    private final long totalEntities;
    private final int pluginCount;
    private final String overallStatus;

    public PerformanceBaseline(
            String createdAt,
            String createdBy,
            String serverVersion,
            String javaVersion,
            String pluginVersion,
            double tps,
            double mspt,
            double memoryUsagePercent,
            int onlinePlayers,
            String loadedWorlds,
            int loadedChunks,
            long totalEntities,
            int pluginCount,
            String overallStatus
    ) {
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.serverVersion = serverVersion;
        this.javaVersion = javaVersion;
        this.pluginVersion = pluginVersion;
        this.tps = tps;
        this.mspt = mspt;
        this.memoryUsagePercent = memoryUsagePercent;
        this.onlinePlayers = onlinePlayers;
        this.loadedWorlds = loadedWorlds;
        this.loadedChunks = loadedChunks;
        this.totalEntities = totalEntities;
        this.pluginCount = pluginCount;
        this.overallStatus = overallStatus;
    }

    public static PerformanceBaseline capture(
            ServerDoctorPlugin plugin,
            PluginConfig pluginConfig,
            RecommendationService recommendationService,
            String createdBy
    ) {
        ServerStats stats = ServerStats.collect(plugin.getEnabledAtMillis());
        ServerHealthStatus health = ServerHealthStatusService.evaluate(
                stats,
                pluginConfig,
                plugin.getLagSpikeHistory(),
                recommendationService
        );

        return new PerformanceBaseline(
                LocalDateTime.now().format(STORAGE_TIME),
                createdBy,
                Bukkit.getVersion(),
                System.getProperty("java.version", "unknown"),
                SpigotApiCompat.pluginVersion(plugin),
                stats.currentTps,
                stats.mspt,
                stats.memoryUsagePercent,
                stats.onlinePlayers,
                stats.worldNames,
                stats.loadedChunkCount,
                stats.entityCount,
                Bukkit.getPluginManager().getPlugins().length,
                health.overall().name()
        );
    }

    public static PerformanceBaseline fromConfig(FileConfiguration config) {
        return new PerformanceBaseline(
                config.getString("created-at", "unknown"),
                config.getString("created-by", "unknown"),
                config.getString("server-version", "unknown"),
                config.getString("java-version", "unknown"),
                config.getString("plugin-version", "unknown"),
                config.getDouble("tps"),
                config.getDouble("mspt"),
                config.getDouble("memory-usage-percent"),
                config.getInt("online-players"),
                config.getString("loaded-worlds", "(none)"),
                config.getInt("loaded-chunks"),
                config.getLong("total-entities"),
                config.getInt("plugin-count"),
                config.getString("overall-status", "UNKNOWN")
        );
    }

    public void save(File file) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Could not create baselines folder: " + parent.getAbsolutePath());
        }

        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("created-at", createdAt);
        yaml.set("created-by", createdBy);
        yaml.set("server-version", serverVersion);
        yaml.set("java-version", javaVersion);
        yaml.set("plugin-version", pluginVersion);
        yaml.set("tps", tps);
        yaml.set("mspt", mspt);
        yaml.set("memory-usage-percent", memoryUsagePercent);
        yaml.set("online-players", onlinePlayers);
        yaml.set("loaded-worlds", loadedWorlds);
        yaml.set("loaded-chunks", loadedChunks);
        yaml.set("total-entities", totalEntities);
        yaml.set("plugin-count", pluginCount);
        yaml.set("overall-status", overallStatus);
        yaml.save(file);
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public String getPluginVersion() {
        return pluginVersion;
    }

    public double getTps() {
        return tps;
    }

    public double getMspt() {
        return mspt;
    }

    public double getMemoryUsagePercent() {
        return memoryUsagePercent;
    }

    public int getOnlinePlayers() {
        return onlinePlayers;
    }

    public String getLoadedWorlds() {
        return loadedWorlds;
    }

    public int getLoadedChunks() {
        return loadedChunks;
    }

    public long getTotalEntities() {
        return totalEntities;
    }

    public int getPluginCount() {
        return pluginCount;
    }

    public String getOverallStatus() {
        return overallStatus;
    }

    static String formatSignedChange(double change, String unit) {
        String sign = change > 0 ? "+" : "";
        if (unit == null || unit.isEmpty()) {
            return String.format(Locale.ROOT, "%s%.2f", sign, change);
        }
        return String.format(Locale.ROOT, "%s%.2f %s", sign, change, unit);
    }

    static String formatSignedLongChange(long change) {
        String sign = change > 0 ? "+" : "";
        return String.format(Locale.ROOT, "%s%,d", sign, change);
    }
}
