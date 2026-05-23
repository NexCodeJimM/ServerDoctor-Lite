package com.jimm.serverdoctor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Snapshot of one loaded chunk's entity and block data for lag analysis.
 */
public final class ChunkAnalysisResult implements Comparable<ChunkAnalysisResult> {

    public final String worldName;
    public final int chunkX;
    public final int chunkZ;
    public final int totalEntities;
    public final int droppedItems;
    public final int mobs;
    public final int tileEntities;
    public final int hoppers;
    public final double heavinessScore;
    private final List<String> recommendations;

    public ChunkAnalysisResult(
            String worldName,
            int chunkX,
            int chunkZ,
            int totalEntities,
            int droppedItems,
            int mobs,
            int tileEntities,
            int hoppers,
            double heavinessScore,
            List<String> recommendations
    ) {
        this.worldName = worldName;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.totalEntities = totalEntities;
        this.droppedItems = droppedItems;
        this.mobs = mobs;
        this.tileEntities = tileEntities;
        this.hoppers = hoppers;
        this.heavinessScore = heavinessScore;
        this.recommendations = Collections.unmodifiableList(new ArrayList<>(recommendations));
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public String chunkCoordinates() {
        return chunkX + ", " + chunkZ;
    }

    @Override
    public int compareTo(ChunkAnalysisResult other) {
        return Double.compare(other.heavinessScore, this.heavinessScore);
    }
}
