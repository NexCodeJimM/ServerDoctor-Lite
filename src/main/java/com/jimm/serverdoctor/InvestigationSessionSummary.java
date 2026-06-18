package com.jimm.serverdoctor;

/**
 * Completed investigation session snapshot (kept until the next session starts).
 */
public final class InvestigationSessionSummary {

    private final long startTimeMillis;
    private final long endTimeMillis;
    private final String startedBy;
    private final boolean autoStopped;

    private final int lagSpikeCount;
    private final double worstTps;
    private final double worstMspt;
    private final double peakMemoryPercent;
    private final int performanceSampleCount;

    private final boolean hasHeaviestChunk;
    private final String heaviestChunkWorld;
    private final int heaviestChunkX;
    private final int heaviestChunkZ;
    private final double heaviestChunkScore;
    private final int heaviestChunkEntities;

    private final int cleanupPreviewCount;
    private final int cleanupConfirmCount;
    private final int totalEntitiesRemovedInCleanup;
    private final int recommendationCount;

    private final String finalRecommendation;

    InvestigationSessionSummary(
            InvestigationSessionData session,
            long endTimeMillis,
            boolean autoStopped,
            String finalRecommendation
    ) {
        this.startTimeMillis = session.getStartTimeMillis();
        this.endTimeMillis = endTimeMillis;
        this.startedBy = session.getStartedBy();
        this.autoStopped = autoStopped;
        this.lagSpikeCount = session.getLagSpikeCount();
        this.worstTps = session.getWorstTps();
        this.worstMspt = session.getWorstMspt();
        this.peakMemoryPercent = session.getPeakMemoryPercent();
        this.performanceSampleCount = session.getPerformanceSampleCount();
        this.hasHeaviestChunk = session.hasHeaviestChunk();
        this.heaviestChunkWorld = session.getHeaviestChunkWorld();
        this.heaviestChunkX = session.getHeaviestChunkX();
        this.heaviestChunkZ = session.getHeaviestChunkZ();
        this.heaviestChunkScore = session.getHeaviestChunkScore();
        this.heaviestChunkEntities = session.getHeaviestChunkEntities();
        this.cleanupPreviewCount = session.getCleanupPreviewCount();
        this.cleanupConfirmCount = session.getCleanupConfirmCount();
        this.totalEntitiesRemovedInCleanup = session.getTotalEntitiesRemovedInCleanup();
        this.recommendationCount = session.getRecommendationCount();
        this.finalRecommendation = finalRecommendation;
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public long getEndTimeMillis() {
        return endTimeMillis;
    }

    public String getStartedBy() {
        return startedBy;
    }

    public boolean isAutoStopped() {
        return autoStopped;
    }

    public long durationMillis() {
        return Math.max(0, endTimeMillis - startTimeMillis);
    }

    public int getLagSpikeCount() {
        return lagSpikeCount;
    }

    public double getWorstTps() {
        return worstTps;
    }

    public double getWorstMspt() {
        return worstMspt;
    }

    public double getPeakMemoryPercent() {
        return peakMemoryPercent;
    }

    public int getPerformanceSampleCount() {
        return performanceSampleCount;
    }

    public boolean hasHeaviestChunk() {
        return hasHeaviestChunk;
    }

    public String getHeaviestChunkWorld() {
        return heaviestChunkWorld;
    }

    public int getHeaviestChunkX() {
        return heaviestChunkX;
    }

    public int getHeaviestChunkZ() {
        return heaviestChunkZ;
    }

    public double getHeaviestChunkScore() {
        return heaviestChunkScore;
    }

    public int getHeaviestChunkEntities() {
        return heaviestChunkEntities;
    }

    public int getCleanupPreviewCount() {
        return cleanupPreviewCount;
    }

    public int getCleanupConfirmCount() {
        return cleanupConfirmCount;
    }

    public int getTotalEntitiesRemovedInCleanup() {
        return totalEntitiesRemovedInCleanup;
    }

    public int getRecommendationCount() {
        return recommendationCount;
    }

    public String getFinalRecommendation() {
        return finalRecommendation;
    }
}
