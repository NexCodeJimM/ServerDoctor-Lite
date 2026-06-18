package com.jimm.serverdoctor;

/**
 * Mutable counters for one active investigation session (in-memory only).
 */
final class InvestigationSessionData {

    private final long startTimeMillis;
    private final String startedBy;

    private int lagSpikeCount;
    private double worstTps = 20.0;
    private double worstMspt;
    private double peakMemoryPercent;
    private int performanceSampleCount;

    private String heaviestChunkWorld;
    private int heaviestChunkX;
    private int heaviestChunkZ;
    private double heaviestChunkScore;
    private int heaviestChunkEntities;
    private boolean hasHeaviestChunk;

    private int cleanupPreviewCount;
    private int cleanupConfirmCount;
    private int totalEntitiesRemovedInCleanup;

    private int recommendationCount;

    InvestigationSessionData(long startTimeMillis, String startedBy) {
        this.startTimeMillis = startTimeMillis;
        this.startedBy = startedBy;
    }

    long getStartTimeMillis() {
        return startTimeMillis;
    }

    String getStartedBy() {
        return startedBy;
    }

    int getLagSpikeCount() {
        return lagSpikeCount;
    }

    double getWorstTps() {
        return worstTps;
    }

    double getWorstMspt() {
        return worstMspt;
    }

    double getPeakMemoryPercent() {
        return peakMemoryPercent;
    }

    int getPerformanceSampleCount() {
        return performanceSampleCount;
    }

    boolean hasHeaviestChunk() {
        return hasHeaviestChunk;
    }

    String getHeaviestChunkWorld() {
        return heaviestChunkWorld;
    }

    int getHeaviestChunkX() {
        return heaviestChunkX;
    }

    int getHeaviestChunkZ() {
        return heaviestChunkZ;
    }

    double getHeaviestChunkScore() {
        return heaviestChunkScore;
    }

    int getHeaviestChunkEntities() {
        return heaviestChunkEntities;
    }

    int getCleanupPreviewCount() {
        return cleanupPreviewCount;
    }

    int getCleanupConfirmCount() {
        return cleanupConfirmCount;
    }

    int getTotalEntitiesRemovedInCleanup() {
        return totalEntitiesRemovedInCleanup;
    }

    int getRecommendationCount() {
        return recommendationCount;
    }

    void recordPerformance(ServerStats stats) {
        performanceSampleCount++;
        if (stats.currentTps < worstTps) {
            worstTps = stats.currentTps;
        }
        if (stats.mspt > worstMspt) {
            worstMspt = stats.mspt;
        }
        if (stats.memoryUsagePercent > peakMemoryPercent) {
            peakMemoryPercent = stats.memoryUsagePercent;
        }
    }

    void recordLagSpike(LagSpikeEvent spike) {
        lagSpikeCount++;
        if (spike.tps() < worstTps) {
            worstTps = spike.tps();
        }
        if (spike.mspt() > worstMspt) {
            worstMspt = spike.mspt();
        }
        if (spike.memoryUsagePercent() > peakMemoryPercent) {
            peakMemoryPercent = spike.memoryUsagePercent();
        }
    }

    void considerHeavyChunk(ChunkAnalysisResult chunk) {
        if (!hasHeaviestChunk || chunk.heavinessScore > heaviestChunkScore) {
            hasHeaviestChunk = true;
            heaviestChunkWorld = chunk.worldName;
            heaviestChunkX = chunk.chunkX;
            heaviestChunkZ = chunk.chunkZ;
            heaviestChunkScore = chunk.heavinessScore;
            heaviestChunkEntities = chunk.totalEntities;
        }
    }

    void recordCleanupPreview() {
        cleanupPreviewCount++;
    }

    void recordCleanupConfirm(CleanupExecuteResult result) {
        cleanupConfirmCount++;
        totalEntitiesRemovedInCleanup += result.totalRemoved();
    }

    void addRecommendations(int count) {
        if (count > 0) {
            recommendationCount += count;
        }
    }
}
