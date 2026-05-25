package com.jimm.serverdoctor;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Removes entities from loaded worlds according to cleanup config and safety rules.
 * Must run on the server main thread.
 */
public final class CleanupExecuteService {

    private CleanupExecuteService() {
    }

    public static boolean hasAnyCategoryEnabled(CleanupConfig config) {
        return config.isIncludeDroppedItems()
                || config.isIncludeHostileMobs()
                || config.isIncludePassiveMobs();
    }

    /**
     * Counts entities that would be removed (respects protection rules).
     */
    public static int countEligibleToRemove(CleanupConfig config) {
        int count = 0;
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (CleanupEntityRules.shouldRemove(entity, config)) {
                    count++;
                }
            }
        }
        return count;
    }

    public static CleanupExecuteResult execute(CleanupConfig config, String executedBy) {
        List<Entity> toRemove = collectRemovableEntities(config);

        int droppedItemsRemoved = 0;
        int hostileMobsRemoved = 0;
        int passiveMobsRemoved = 0;

        for (Entity entity : toRemove) {
            if (entity instanceof Item) {
                droppedItemsRemoved++;
            } else if (entity instanceof Monster) {
                hostileMobsRemoved++;
            } else if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                passiveMobsRemoved++;
            }
            entity.remove();
        }

        int totalRemoved = droppedItemsRemoved + hostileMobsRemoved + passiveMobsRemoved;
        int worldsScanned = Bukkit.getWorlds().size();

        return new CleanupExecuteResult(
                LocalDateTime.now(),
                executedBy,
                worldsScanned,
                droppedItemsRemoved,
                hostileMobsRemoved,
                passiveMobsRemoved,
                totalRemoved
        );
    }

    private static List<Entity> collectRemovableEntities(CleanupConfig config) {
        List<Entity> removable = new ArrayList<>();

        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (CleanupEntityRules.shouldRemove(entity, config)) {
                    removable.add(entity);
                }
            }
        }

        return removable;
    }
}
