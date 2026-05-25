package com.jimm.serverdoctor;

/**
 * Entity counts from a cleanup preview scan (no entities removed).
 */
public record CleanupPreviewResult(
        int worldsScanned,
        int droppedItems,
        int hostileMobs,
        int passiveMobs,
        int totalLivingEntities,
        int players
) {
}
