package com.jimm.serverdoctor;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Version-safe helpers for Spigot/Paper from 1.21.x through 26.1.x.
 * Uses reflection for APIs that were added in newer releases.
 */
public final class SpigotApiCompat {

    private SpigotApiCompat() {
    }

    public static String pluginVersion(JavaPlugin plugin) {
        return readPluginVersion(plugin, plugin.getDescription().getVersion());
    }

    public static String pluginVersion(Plugin plugin) {
        return readPluginVersion(plugin, plugin.getDescription().getVersion());
    }

    private static String readPluginVersion(Plugin plugin, String fallback) {
        try {
            Object meta = plugin.getClass().getMethod("getPluginMeta").invoke(plugin);
            Object version = meta.getClass().getMethod("getVersion").invoke(meta);
            if (version instanceof String text && !text.isBlank()) {
                return text;
            }
        } catch (ReflectiveOperationException ignored) {
            // getPluginMeta() — newer servers; getDescription() works on 1.21.x
        }
        return fallback;
    }

    public static boolean hasCustomName(Entity entity) {
        if (entity.getCustomName() != null) {
            return true;
        }
        try {
            Object result = entity.getClass().getMethod("customName").invoke(entity);
            return result != null;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    public static String minecraftVersion() {
        try {
            Object result = Bukkit.class.getMethod("getMinecraftVersion").invoke(null);
            if (result instanceof String text && !text.isBlank()) {
                return text;
            }
        } catch (ReflectiveOperationException ignored) {
            // getMinecraftVersion() — newer; Bukkit version string works on 1.21.x
        }
        return Bukkit.getBukkitVersion();
    }

    public static String serverSoftwareSummary() {
        return Bukkit.getName() + " " + minecraftVersion();
    }

    public static double readCurrentTps() {
        try {
            Object server = Bukkit.getServer();
            Object result = server.getClass().getMethod("getTPS").invoke(server);
            if (result instanceof double[] samples && samples.length > 0) {
                return samples[0];
            }
        } catch (ReflectiveOperationException ignored) {
            // Unavailable on very old forks — safe default below
        }
        return 20.0;
    }

    public static double readMspt() {
        try {
            Object server = Bukkit.getServer();
            Object result = server.getClass().getMethod("getAverageTickTime").invoke(server);
            if (result instanceof Number number) {
                return number.doubleValue();
            }
        } catch (ReflectiveOperationException ignored) {
            // MSPT may be missing on some 1.21.x builds — estimate from TPS
        }
        double tps = readCurrentTps();
        if (tps <= 0.0) {
            return 50.0;
        }
        return Math.min(1000.0, 1000.0 / tps);
    }
}
