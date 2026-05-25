package com.jimm.serverdoctor;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Teleports players to the center of a chunk at a safe Y level.
 */
public final class ChunkTeleportService {

    private ChunkTeleportService() {
    }

    /**
     * Command string for clickable chat (run as the clicking player).
     */
    public static String buildRunCommand(String worldName, int chunkX, int chunkZ) {
        String worldArg = worldName.contains(" ") ? "\"" + worldName + "\"" : worldName;
        return String.format("/doctor tpchunk %s %d %d", worldArg, chunkX, chunkZ);
    }

    public static TeleportResult teleportToChunk(Player player, String worldName, int chunkX, int chunkZ) {
        String resolvedWorldName = normalizeWorldName(worldName);
        World world = Bukkit.getWorld(resolvedWorldName);
        if (world == null) {
            return TeleportResult.failure("World not found: " + resolvedWorldName);
        }

        int blockX = chunkX * 16 + 8;
        int blockZ = chunkZ * 16 + 8;
        int y = world.getHighestBlockYAt(blockX, blockZ) + 1;
        Location location = new Location(world, blockX + 0.5, y, blockZ + 0.5);

        if (!player.teleport(location)) {
            return TeleportResult.failure("Teleport failed. Try again or check world permissions.");
        }

        return TeleportResult.success(resolvedWorldName, chunkX, chunkZ, blockX, y, blockZ);
    }

    static String normalizeWorldName(String worldName) {
        if (worldName == null) {
            return "";
        }
        String trimmed = worldName.trim();
        if (trimmed.length() >= 2 && trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }

    public record TeleportResult(boolean success, String message, String worldName, int chunkX, int chunkZ, int blockX, int y, int blockZ) {

        static TeleportResult success(String worldName, int chunkX, int chunkZ, int blockX, int y, int blockZ) {
            return new TeleportResult(true, "", worldName, chunkX, chunkZ, blockX, y, blockZ);
        }

        static TeleportResult failure(String message) {
            return new TeleportResult(false, message, "", 0, 0, 0, 0, 0);
        }
    }
}
