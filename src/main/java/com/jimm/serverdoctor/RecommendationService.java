package com.jimm.serverdoctor;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Generates short, beginner-friendly advice based on server health and chunk scans.
 * All suggestions are advisory — nothing is changed automatically.
 */
public final class RecommendationService {

    public boolean isEnabled(PluginConfig config) {
        return config.isRecommendationsEnabled();
    }

    /**
     * Recommendations for {@code /doctor report} and export (server-wide).
     */
    public List<String> forHealthReport(ServerStats stats, PluginConfig config) {
        if (!isEnabled(config)) {
            return List.of();
        }

        Set<String> tips = new LinkedHashSet<>();
        List<AlertType> active = HealthChecker.findActiveAlerts(stats, config);

        if (active.contains(AlertType.TPS)) {
            tips.add("Run /doctor chunks to locate overloaded areas.");
            tips.add("Check farms, grinders, and redstone that run every tick.");
            tips.add("Review new plugins or world changes if TPS dropped recently.");
        }

        if (active.contains(AlertType.MSPT)) {
            tips.add("High MSPT means each server tick takes longer — CPU is working harder.");
            tips.add("Run /doctor chunks to find busy loaded chunks.");
            tips.add("Consider lowering simulation-distance in server.properties.");
        }

        if (active.contains(AlertType.MEMORY)) {
            tips.add("Raise -Xmx in your server start script if RAM is often near the limit.");
            tips.add("Plan a restart during low traffic after large updates or plugin changes.");
            tips.add("Check for heavy plugins, large worlds, or backup tools using extra RAM.");
        }

        if (active.contains(AlertType.ENTITIES)) {
            tips.add("Check farms, grinders, or item collection systems.");
            tips.add("Spread mob farms across areas instead of stacking many in one chunk.");
            tips.add("Run /doctor chunks to see which loaded areas hold the most entities.");
        }

        if (active.contains(AlertType.CHUNKS)) {
            tips.add("Consider lowering simulation-distance in server.properties.");
            tips.add("Keep players closer together if too many chunks stay loaded.");
            tips.add("Use world borders or limit unused dimensions if chunk count stays high.");
        }

        if (stats.droppedItemCount >= serverDroppedItemLimit(config)) {
            tips.add("Many dropped items are loaded — clear ground clutter near farms and mob grinders.");
            tips.add("Check item sorters and collection systems that may overflow.");
        }

        if (stats.mobCount >= serverMobLimit(config)) {
            tips.add("High mob count — inspect mob farms, breeders, and spawn-heavy areas.");
            tips.add("Reduce unnecessary mob spawning in always-loaded chunks.");
        }

        if (stats.hopperCount >= serverHopperLimit(config)) {
            tips.add("Many hoppers are loaded — long hopper chains are costly.");
            tips.add("Consider water streams, fewer storage hoppers, or compacting designs.");
        }

        if (shouldSuggestChunkScan(active, stats, config)) {
            tips.add("Run /doctor chunks to locate overloaded areas.");
        }

        return new ArrayList<>(tips);
    }

    /**
     * Per-chunk tips for {@code /doctor chunks}.
     */
    public List<String> forChunk(
            int totalEntities,
            int droppedItems,
            int mobs,
            int hoppers,
            PluginConfig config
    ) {
        if (!isEnabled(config)) {
            return List.of();
        }

        Set<String> tips = new LinkedHashSet<>();

        if (droppedItems >= config.getChunkWarningDroppedItemLimit()) {
            tips.add("Possible item buildup — check farms, grinders, or item collection systems.");
        }
        if (mobs >= config.getChunkWarningEntityLimit() / 2) {
            tips.add("Possible mob farm or crowded spawn area — visit these coordinates in-game.");
        }
        if (hoppers >= config.getChunkWarningHopperLimit()) {
            tips.add("Possible storage or redstone-heavy system — review hopper chains here.");
        }
        if (totalEntities >= config.getChunkWarningEntityLimit()) {
            tips.add("Entity-heavy chunk — reduce spawners or entity farms in this area.");
        }

        if (tips.isEmpty()) {
            tips.add("Moderate load — monitor this area if lag continues.");
        }

        return new ArrayList<>(tips);
    }

    public List<String> forChunk(ChunkAnalysisResult result, PluginConfig config) {
        return forChunk(
                result.totalEntities,
                result.droppedItems,
                result.mobs,
                result.hoppers,
                config
        );
    }

    /**
     * Overall tips after a chunk scan (shown once at the end of {@code /doctor chunks}).
     */
    public List<String> forChunksOverview(List<ChunkAnalysisResult> topResults, PluginConfig config) {
        if (!isEnabled(config) || topResults.isEmpty()) {
            return List.of();
        }

        Set<String> tips = new LinkedHashSet<>();
        int heavyItemChunks = 0;
        int heavyMobChunks = 0;
        int heavyHopperChunks = 0;

        for (ChunkAnalysisResult result : topResults) {
            if (result.droppedItems >= config.getChunkWarningDroppedItemLimit()) {
                heavyItemChunks++;
            }
            if (result.mobs >= config.getChunkWarningEntityLimit() / 2) {
                heavyMobChunks++;
            }
            if (result.hoppers >= config.getChunkWarningHopperLimit()) {
                heavyHopperChunks++;
            }
        }

        if (heavyItemChunks > 0) {
            tips.add("Top chunks include item buildup — inspect farms and ground drops at listed coordinates.");
        }
        if (heavyMobChunks > 0) {
            tips.add("Top chunks include many mobs — check mob farms near the listed coordinates.");
        }
        if (heavyHopperChunks > 0) {
            tips.add("Top chunks include many hoppers — simplify storage and redstone near those areas.");
        }

        if (topResults.getFirst().heavinessScore >= 2.0) {
            tips.add("Heaviest chunks may be driving lag — focus fixes on rank #1 first.");
        }

        tips.add("Recommendations are advisory only — review areas in-game before changing builds.");

        return new ArrayList<>(tips);
    }

    private static boolean shouldSuggestChunkScan(
            List<AlertType> active,
            ServerStats stats,
            PluginConfig config
    ) {
        if (active.contains(AlertType.TPS) || active.contains(AlertType.MSPT) || active.contains(AlertType.CHUNKS)) {
            return false;
        }
        return stats.droppedItemCount >= serverDroppedItemLimit(config)
                || stats.mobCount >= serverMobLimit(config)
                || stats.hopperCount >= serverHopperLimit(config)
                || stats.entityCount > config.getEntityWarningLimit() / 2;
    }

    private static int serverDroppedItemLimit(PluginConfig config) {
        return Math.max(200, config.getChunkWarningDroppedItemLimit() * 5);
    }

    private static long serverMobLimit(PluginConfig config) {
        return Math.max(500L, config.getEntityWarningLimit() / 3);
    }

    private static int serverHopperLimit(PluginConfig config) {
        return Math.max(100, config.getChunkWarningHopperLimit() * 5);
    }
}
