package com.jimm.serverdoctor;

import org.bukkit.Bukkit;
import org.bukkit.World;

public final class ServerStats {

    public final int onlinePlayers;
    public final int maxPlayers;
    public final long usedBytes;
    public final long maxBytes;
    public final double memoryUsagePercent;
    public final String worldNames;
    public final long entityCount;
    public final int loadedChunkCount;
    public final String uptime;
    public final double currentTps;
    public final double mspt;

    private ServerStats(
            int onlinePlayers,
            int maxPlayers,
            long usedBytes,
            long maxBytes,
            double memoryUsagePercent,
            String worldNames,
            long entityCount,
            int loadedChunkCount,
            String uptime,
            double currentTps,
            double mspt
    ) {
        this.onlinePlayers = onlinePlayers;
        this.maxPlayers = maxPlayers;
        this.usedBytes = usedBytes;
        this.maxBytes = maxBytes;
        this.memoryUsagePercent = memoryUsagePercent;
        this.worldNames = worldNames;
        this.entityCount = entityCount;
        this.loadedChunkCount = loadedChunkCount;
        this.uptime = uptime;
        this.currentTps = currentTps;
        this.mspt = mspt;
    }

    public static ServerStats collect(long pluginEnabledAtMillis) {
        Runtime runtime = Runtime.getRuntime();
        long usedBytes = runtime.totalMemory() - runtime.freeMemory();
        long maxBytes = runtime.maxMemory();

        double memoryPercent = 0.0;
        if (maxBytes > 0) {
            memoryPercent = (usedBytes * 100.0) / maxBytes;
        }

        return new ServerStats(
                Bukkit.getOnlinePlayers().size(),
                Bukkit.getMaxPlayers(),
                usedBytes,
                maxBytes,
                memoryPercent,
                formatWorldNames(),
                countEntitiesInAllWorlds(),
                countLoadedChunksInAllWorlds(),
                formatUptime(System.currentTimeMillis() - pluginEnabledAtMillis),
                ServerPerformance.readCurrentTps(),
                ServerPerformance.readMspt()
        );
    }

    static String formatUptime(long millis) {
        long totalSeconds = millis / 1000;
        long days = totalSeconds / 86_400;
        long hours = (totalSeconds % 86_400) / 3_600;
        long minutes = (totalSeconds % 3_600) / 60;
        long seconds = totalSeconds % 60;

        if (days > 0) {
            return String.format("%dd %dh %dm %ds", days, hours, minutes, seconds);
        }
        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        }
        if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        }
        return seconds + "s";
    }

    static String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        double kilobytes = bytes / 1024.0;
        if (kilobytes < 1024) {
            return String.format("%.1f KB", kilobytes);
        }
        double megabytes = kilobytes / 1024.0;
        if (megabytes < 1024) {
            return String.format("%.1f MB", megabytes);
        }
        double gigabytes = megabytes / 1024.0;
        return String.format("%.2f GB", gigabytes);
    }

    static String formatTps(double tps) {
        return String.format("%.2f", tps);
    }

    static String formatMspt(double mspt) {
        return String.format("%.2f ms", mspt);
    }

    private static String formatWorldNames() {
        if (Bukkit.getWorlds().isEmpty()) {
            return "(none)";
        }
        StringBuilder names = new StringBuilder();
        for (World world : Bukkit.getWorlds()) {
            if (names.length() > 0) {
                names.append(", ");
            }
            names.append(world.getName());
        }
        return names.toString();
    }

    private static long countEntitiesInAllWorlds() {
        long total = 0;
        for (World world : Bukkit.getWorlds()) {
            total += world.getEntities().size();
        }
        return total;
    }

    private static int countLoadedChunksInAllWorlds() {
        int total = 0;
        for (World world : Bukkit.getWorlds()) {
            total += world.getLoadedChunks().length;
        }
        return total;
    }
}
