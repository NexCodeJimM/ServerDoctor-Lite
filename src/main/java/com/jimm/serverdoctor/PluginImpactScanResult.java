package com.jimm.serverdoctor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Output of a plugin impact scan for {@code /doctor plugins}.
 */
public final class PluginImpactScanResult {

    private final int totalPlugins;
    private final PluginCountCategory countCategory;
    private final PaperTimingsStatus timingsStatus;
    private final List<PluginImpactMetrics> plugins;
    private final List<String> worthReviewNotes;
    private final List<String> recommendations;

    public PluginImpactScanResult(
            int totalPlugins,
            PluginCountCategory countCategory,
            PaperTimingsStatus timingsStatus,
            List<PluginImpactMetrics> plugins,
            List<String> worthReviewNotes,
            List<String> recommendations
    ) {
        this.totalPlugins = totalPlugins;
        this.countCategory = countCategory;
        this.timingsStatus = timingsStatus;
        this.plugins = List.copyOf(plugins);
        this.worthReviewNotes = List.copyOf(worthReviewNotes);
        this.recommendations = List.copyOf(recommendations);
    }

    public int getTotalPlugins() {
        return totalPlugins;
    }

    public PluginCountCategory getCountCategory() {
        return countCategory;
    }

    public PaperTimingsStatus getTimingsStatus() {
        return timingsStatus;
    }

    public List<PluginImpactMetrics> getPlugins() {
        return plugins;
    }

    public List<String> getWorthReviewNotes() {
        return worthReviewNotes;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public List<PluginImpactMetrics> pluginsSortedByActivity() {
        List<PluginImpactMetrics> sorted = new ArrayList<>(plugins);
        Collections.sort(sorted);
        return sorted;
    }
}
