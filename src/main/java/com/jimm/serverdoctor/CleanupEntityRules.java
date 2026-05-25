package com.jimm.serverdoctor;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Warden;

/**
 * Shared rules for what cleanup may affect. Used by preview counts and confirm execution.
 */
public final class CleanupEntityRules {

    private CleanupEntityRules() {
    }

    public static boolean isProtected(Entity entity) {
        if (entity instanceof Player) {
            return true;
        }
        if (entity.customName() != null) {
            return true;
        }
        if (entity instanceof Tameable tameable && tameable.isTamed()) {
            return true;
        }
        if (entity instanceof Villager) {
            return true;
        }
        if (entity instanceof ArmorStand) {
            return true;
        }
        if (entity instanceof ItemFrame) {
            return true;
        }
        if (entity instanceof Minecart) {
            return true;
        }
        if (entity instanceof Boat) {
            return true;
        }
        if (entity instanceof Boss || entity instanceof Warden) {
            return true;
        }
        return false;
    }

    public static boolean matchesCleanupCategory(Entity entity, CleanupConfig config) {
        if (entity instanceof Item) {
            return config.isIncludeDroppedItems();
        }
        if (!(entity instanceof LivingEntity livingEntity) || livingEntity instanceof Player) {
            return false;
        }
        if (livingEntity instanceof Monster) {
            return config.isIncludeHostileMobs();
        }
        return config.isIncludePassiveMobs();
    }

    public static boolean shouldRemove(Entity entity, CleanupConfig config) {
        return matchesCleanupCategory(entity, config) && !isProtected(entity);
    }
}
