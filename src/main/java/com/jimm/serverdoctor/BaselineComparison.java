package com.jimm.serverdoctor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Result of comparing current server stats against a saved baseline.
 */
public final class BaselineComparison {

    private final double tpsChange;
    private final BaselineMetricTrend tpsTrend;
    private final double msptChange;
    private final BaselineMetricTrend msptTrend;
    private final double memoryChange;
    private final BaselineMetricTrend memoryTrend;
    private final long entityChange;
    private final BaselineMetricTrend entityTrend;
    private final int chunkChange;
    private final BaselineMetricTrend chunkTrend;
    private final int pluginChange;
    private final BaselineMetricTrend pluginTrend;
    private final BaselineMetricTrend overallTrend;
    private final List<String> recommendations;

    public BaselineComparison(
            double tpsChange,
            BaselineMetricTrend tpsTrend,
            double msptChange,
            BaselineMetricTrend msptTrend,
            double memoryChange,
            BaselineMetricTrend memoryTrend,
            long entityChange,
            BaselineMetricTrend entityTrend,
            int chunkChange,
            BaselineMetricTrend chunkTrend,
            int pluginChange,
            BaselineMetricTrend pluginTrend,
            BaselineMetricTrend overallTrend,
            List<String> recommendations
    ) {
        this.tpsChange = tpsChange;
        this.tpsTrend = tpsTrend;
        this.msptChange = msptChange;
        this.msptTrend = msptTrend;
        this.memoryChange = memoryChange;
        this.memoryTrend = memoryTrend;
        this.entityChange = entityChange;
        this.entityTrend = entityTrend;
        this.chunkChange = chunkChange;
        this.chunkTrend = chunkTrend;
        this.pluginChange = pluginChange;
        this.pluginTrend = pluginTrend;
        this.overallTrend = overallTrend;
        this.recommendations = List.copyOf(recommendations);
    }

    public double getTpsChange() {
        return tpsChange;
    }

    public BaselineMetricTrend getTpsTrend() {
        return tpsTrend;
    }

    public double getMsptChange() {
        return msptChange;
    }

    public BaselineMetricTrend getMsptTrend() {
        return msptTrend;
    }

    public double getMemoryChange() {
        return memoryChange;
    }

    public BaselineMetricTrend getMemoryTrend() {
        return memoryTrend;
    }

    public long getEntityChange() {
        return entityChange;
    }

    public BaselineMetricTrend getEntityTrend() {
        return entityTrend;
    }

    public int getChunkChange() {
        return chunkChange;
    }

    public BaselineMetricTrend getChunkTrend() {
        return chunkTrend;
    }

    public int getPluginChange() {
        return pluginChange;
    }

    public BaselineMetricTrend getPluginTrend() {
        return pluginTrend;
    }

    public BaselineMetricTrend getOverallTrend() {
        return overallTrend;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public static BaselineComparison compare(
            ServerStats current,
            PerformanceBaseline baseline,
            BaselineConfig config
    ) {
        double tpsChange = current.currentTps - baseline.getTps();
        BaselineMetricTrend tpsTrend = classifyTps(tpsChange, config);

        double msptChange = current.mspt - baseline.getMspt();
        BaselineMetricTrend msptTrend = classifyMspt(msptChange, config);

        double memoryChange = current.memoryUsagePercent - baseline.getMemoryUsagePercent();
        BaselineMetricTrend memoryTrend = classifyMemory(memoryChange, config);

        long entityChange = current.entityCount - baseline.getTotalEntities();
        BaselineMetricTrend entityTrend = classifyEntity(entityChange, config);

        int chunkChange = current.loadedChunkCount - baseline.getLoadedChunks();
        BaselineMetricTrend chunkTrend = classifyChunk(chunkChange, config);

        int pluginChange = countPlugins() - baseline.getPluginCount();
        BaselineMetricTrend pluginTrend = classifyPlugin(pluginChange);

        BaselineMetricTrend overallTrend = summarizeOverall(
                tpsTrend, msptTrend, memoryTrend, entityTrend, chunkTrend, pluginTrend
        );

        List<String> recommendations = buildRecommendations(
                entityTrend, entityChange, config,
                msptTrend,
                pluginTrend, pluginChange,
                memoryTrend, memoryChange
        );

        return new BaselineComparison(
                tpsChange, tpsTrend,
                msptChange, msptTrend,
                memoryChange, memoryTrend,
                entityChange, entityTrend,
                chunkChange, chunkTrend,
                pluginChange, pluginTrend,
                overallTrend,
                recommendations
        );
    }

    private static int countPlugins() {
        return org.bukkit.Bukkit.getPluginManager().getPlugins().length;
    }

    private static BaselineMetricTrend classifyTps(double change, BaselineConfig config) {
        if (change <= -config.getSignificantTpsDrop()) {
            return BaselineMetricTrend.DEGRADED;
        }
        if (change >= config.getSignificantTpsDrop() * 0.5) {
            return BaselineMetricTrend.IMPROVED;
        }
        return BaselineMetricTrend.STABLE;
    }

    private static BaselineMetricTrend classifyMspt(double change, BaselineConfig config) {
        if (change >= config.getSignificantMsptIncrease()) {
            return BaselineMetricTrend.DEGRADED;
        }
        if (change <= -config.getSignificantMsptIncrease() * 0.5) {
            return BaselineMetricTrend.IMPROVED;
        }
        return BaselineMetricTrend.STABLE;
    }

    private static BaselineMetricTrend classifyMemory(double change, BaselineConfig config) {
        if (change >= config.getSignificantMemoryIncreasePercent()) {
            return BaselineMetricTrend.DEGRADED;
        }
        if (change <= -config.getSignificantMemoryIncreasePercent() * 0.5) {
            return BaselineMetricTrend.IMPROVED;
        }
        return BaselineMetricTrend.STABLE;
    }

    private static BaselineMetricTrend classifyEntity(long change, BaselineConfig config) {
        if (change >= config.getSignificantEntityIncrease()) {
            return BaselineMetricTrend.DEGRADED;
        }
        if (change <= -config.getSignificantEntityIncrease()) {
            return BaselineMetricTrend.IMPROVED;
        }
        return BaselineMetricTrend.STABLE;
    }

    private static BaselineMetricTrend classifyChunk(int change, BaselineConfig config) {
        if (change >= config.getSignificantChunkIncrease()) {
            return BaselineMetricTrend.DEGRADED;
        }
        if (change <= -config.getSignificantChunkIncrease()) {
            return BaselineMetricTrend.IMPROVED;
        }
        return BaselineMetricTrend.STABLE;
    }

    private static BaselineMetricTrend classifyPlugin(int change) {
        if (change > 0) {
            return BaselineMetricTrend.DEGRADED;
        }
        if (change < 0) {
            return BaselineMetricTrend.IMPROVED;
        }
        return BaselineMetricTrend.STABLE;
    }

    private static BaselineMetricTrend summarizeOverall(BaselineMetricTrend... trends) {
        int degraded = 0;
        int improved = 0;
        for (BaselineMetricTrend trend : trends) {
            if (trend == BaselineMetricTrend.DEGRADED) {
                degraded++;
            } else if (trend == BaselineMetricTrend.IMPROVED) {
                improved++;
            }
        }
        if (degraded > improved && degraded > 0) {
            return BaselineMetricTrend.DEGRADED;
        }
        if (improved > degraded && improved > 0) {
            return BaselineMetricTrend.IMPROVED;
        }
        return BaselineMetricTrend.STABLE;
    }

    private static List<String> buildRecommendations(
            BaselineMetricTrend entityTrend,
            long entityChange,
            BaselineConfig config,
            BaselineMetricTrend msptTrend,
            BaselineMetricTrend pluginTrend,
            int pluginChange,
            BaselineMetricTrend memoryTrend,
            double memoryChange
    ) {
        List<String> tips = new ArrayList<>();

        if (entityTrend == BaselineMetricTrend.DEGRADED
                && entityChange >= config.getSignificantEntityIncrease()) {
            tips.add("Entity count rose significantly — may indicate buildup worth reviewing with &f/doctor chunks&7.");
        }
        if (msptTrend == BaselineMetricTrend.DEGRADED) {
            tips.add("MSPT worsened vs baseline — try &f/doctor report&7 and &f/doctor plugins&7 for possible contributors.");
        }
        if (pluginTrend == BaselineMetricTrend.DEGRADED && pluginChange > 0) {
            tips.add("Plugin count increased — new plugins may be a possible contributor; review with &f/doctor plugins&7.");
        }
        if (memoryTrend == BaselineMetricTrend.DEGRADED
                && memoryChange >= config.getSignificantMemoryIncreasePercent()) {
            tips.add("Memory usage rose notably — worth reviewing recent world, plugin, or player changes.");
        }
        if (tips.isEmpty()) {
            tips.add("No major baseline shifts detected — keep monitoring if players still report issues.");
        }

        return tips;
    }

    static String trendSummary(BaselineMetricTrend trend) {
        return switch (trend) {
            case IMPROVED -> "&a" + trend.label();
            case STABLE -> "&e" + trend.label();
            case DEGRADED -> "&c" + trend.label();
        };
    }

    static String overallSummary(BaselineMetricTrend trend) {
        return switch (trend) {
            case IMPROVED -> "&aOverall: improved vs baseline.";
            case STABLE -> "&eOverall: stable vs baseline.";
            case DEGRADED -> "&cOverall: degraded vs baseline — review changes below.";
        };
    }
}
