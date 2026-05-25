package com.jimm.serverdoctor;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

/**
 * Scans loaded worlds and counts entities for cleanup preview.
 * Does not remove anything.
 */
public final class CleanupPreviewService {

    private CleanupPreviewService() {
    }

    /**
     * Must run on the server main thread.
     */
    public static CleanupPreviewResult scanLoadedWorlds() {
        int droppedItems = 0;
        int hostileMobs = 0;
        int passiveMobs = 0;
        int totalLivingEntities = 0;
        int players = 0;
        int worldsScanned = Bukkit.getWorlds().size();

        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Item) {
                    droppedItems++;
                    continue;
                }

                if (!(entity instanceof LivingEntity livingEntity)) {
                    continue;
                }

                totalLivingEntities++;

                if (livingEntity instanceof Player) {
                    players++;
                    continue;
                }

                if (livingEntity instanceof Monster) {
                    hostileMobs++;
                } else {
                    passiveMobs++;
                }
            }
        }

        return new CleanupPreviewResult(
                worldsScanned,
                droppedItems,
                hostileMobs,
                passiveMobs,
                totalLivingEntities,
                players
        );
    }
}
