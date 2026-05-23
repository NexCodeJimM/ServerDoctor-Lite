package com.jimm.serverdoctor;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Scans all currently loaded chunks and ranks them by a configurable heaviness score.
 * <p>
 * Must run on the server main thread (Bukkit entity/chunk APIs are not thread-safe).
 */
public final class ChunkAnalyzerService {

    private final PluginConfig pluginConfig;

    public ChunkAnalyzerService(PluginConfig pluginConfig) {
        this.pluginConfig = pluginConfig;
    }

    /**
     * Analyzes every loaded chunk in every loaded world.
     *
     * @return all chunk results, sorted heaviest first
     */
    public List<ChunkAnalysisResult> analyzeLoadedChunks() {
        List<ChunkAnalysisResult> results = new ArrayList<>();

        for (World world : org.bukkit.Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                results.add(analyzeChunk(world, chunk));
            }
        }

        Collections.sort(results);
        return results;
    }

    public List<ChunkAnalysisResult> getTopHeaviestChunks(List<ChunkAnalysisResult> allResults) {
        int limit = Math.max(1, pluginConfig.getChunkAnalyzerTopLimit());
        if (allResults.size() <= limit) {
            return new ArrayList<>(allResults);
        }
        return new ArrayList<>(allResults.subList(0, limit));
    }

    private ChunkAnalysisResult analyzeChunk(World world, Chunk chunk) {
        Entity[] entities = chunk.getEntities();
        int totalEntities = entities.length;
        int droppedItems = 0;
        int mobs = 0;

        for (Entity entity : entities) {
            if (entity instanceof Item) {
                droppedItems++;
            } else if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                mobs++;
            }
        }
        int tileEntities = 0;
        int hoppers = 0;

        for (BlockState blockState : chunk.getTileEntities()) {
            tileEntities++;
            if (blockState instanceof Hopper) {
                hoppers++;
            }
        }

        double score = calculateHeavinessScore(totalEntities, droppedItems, mobs, hoppers);
        List<String> recommendations = buildRecommendations(
                totalEntities,
                droppedItems,
                mobs,
                hoppers
        );

        return new ChunkAnalysisResult(
                world.getName(),
                chunk.getX(),
                chunk.getZ(),
                totalEntities,
                droppedItems,
                mobs,
                tileEntities,
                hoppers,
                score,
                recommendations
        );
    }

    private double calculateHeavinessScore(int totalEntities, int droppedItems, int mobs, int hoppers) {
        double score = 0.0;

        int entityLimit = pluginConfig.getChunkWarningEntityLimit();
        int itemLimit = pluginConfig.getChunkWarningDroppedItemLimit();
        int hopperLimit = pluginConfig.getChunkWarningHopperLimit();

        if (entityLimit > 0) {
            score += (double) totalEntities / entityLimit;
        }
        if (itemLimit > 0) {
            score += (double) droppedItems / itemLimit;
        }
        if (hopperLimit > 0) {
            score += (double) hoppers / hopperLimit;
        }
        if (entityLimit > 0) {
            score += (double) mobs / entityLimit * 0.5;
        }

        return score;
    }

    private List<String> buildRecommendations(int totalEntities, int droppedItems, int mobs, int hoppers) {
        List<String> tips = new ArrayList<>();

        if (droppedItems >= pluginConfig.getChunkWarningDroppedItemLimit()) {
            tips.add("Possible item buildup. Check farms, grinders, or item collection systems.");
        }
        if (mobs >= pluginConfig.getChunkWarningEntityLimit() / 2) {
            tips.add("Possible mob farm or overcrowded area.");
        }
        if (hoppers >= pluginConfig.getChunkWarningHopperLimit()) {
            tips.add("Possible storage or redstone-heavy system.");
        }
        if (totalEntities >= pluginConfig.getChunkWarningEntityLimit()) {
            tips.add("Possible entity-heavy chunk.");
        }

        if (tips.isEmpty()) {
            tips.add("Moderate load — monitor if lag continues.");
        }

        return tips;
    }
}
