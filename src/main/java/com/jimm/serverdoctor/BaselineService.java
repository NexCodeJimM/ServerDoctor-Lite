package com.jimm.serverdoctor;

import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;

/**
 * Manages performance baselines and comparison commands.
 */
public final class BaselineService {

    private final ServerDoctorPlugin plugin;
    private final PluginConfig pluginConfig;
    private final RecommendationService recommendationService;
    private final BaselineStore store;

    private PerformanceBaseline cachedBaseline;

    public BaselineService(
            ServerDoctorPlugin plugin,
            PluginConfig pluginConfig,
            RecommendationService recommendationService
    ) {
        this.plugin = plugin;
        this.pluginConfig = pluginConfig;
        this.recommendationService = recommendationService;
        this.store = new BaselineStore(plugin);
    }

    public void loadFromDisk() {
        cachedBaseline = store.load();
    }

    public boolean hasBaseline() {
        return cachedBaseline != null;
    }

    public PerformanceBaseline getBaseline() {
        return cachedBaseline;
    }

    public void create(CommandSender sender) {
        BaselineConfig config = pluginConfig.getBaseline();
        if (!config.isEnabled()) {
            MessageUtil.send(sender, MessageUtil.error("&cBaseline system is disabled in config.yml."));
            return;
        }

        PerformanceBaseline baseline = PerformanceBaseline.capture(
                plugin,
                pluginConfig,
                recommendationService,
                sender.getName()
        );

        try {
            store.save(baseline);
            cachedBaseline = baseline;
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save performance baseline", exception);
            MessageUtil.send(sender, MessageUtil.error("&cFailed to save baseline. Check the server console."));
            return;
        }

        MessageUtil.sendHeader(sender, "Baseline Created");
        MessageUtil.send(sender, MessageUtil.info(String.format(
                Locale.ROOT,
                "&aSaved performance baseline to &fplugins/ServerDoctor/baselines/%s&a.",
                BaselineStore.DEFAULT_FILE_NAME
        )));
        MessageUtil.sendStat(sender, "Created at", baseline.getCreatedAt());
        MessageUtil.sendStat(sender, "TPS", ServerStats.formatTps(baseline.getTps()));
        MessageUtil.sendStat(sender, "MSPT", ServerStats.formatMspt(baseline.getMspt()));
        MessageUtil.sendStat(sender, "Memory", String.format(Locale.ROOT, "%.1f%%", baseline.getMemoryUsagePercent()));
        MessageUtil.sendStat(sender, "Entities", String.format(Locale.ROOT, "%,d", baseline.getTotalEntities()));
        MessageUtil.sendStat(sender, "Loaded chunks", String.valueOf(baseline.getLoadedChunks()));
        MessageUtil.sendStat(sender, "Overall status", baseline.getOverallStatus());
        MessageUtil.send(sender, MessageUtil.info("&7Compare later with &f/doctor baseline compare&7."));
        MessageUtil.sendFooter(sender);

        plugin.getLogger().info(String.format(
                Locale.ROOT,
                "Performance baseline created by %s (TPS %s, MSPT %s).",
                baseline.getCreatedBy(),
                ServerStats.formatTps(baseline.getTps()),
                ServerStats.formatMspt(baseline.getMspt())
        ));
    }

    public void sendStatus(CommandSender sender) {
        MessageUtil.sendHeader(sender, "Baseline Status");
        BaselineConfig config = pluginConfig.getBaseline();
        MessageUtil.sendStat(sender, "Feature enabled", config.isEnabled() ? "yes" : "no");
        MessageUtil.sendStat(sender, "Baseline file", "plugins/ServerDoctor/baselines/" + BaselineStore.DEFAULT_FILE_NAME);

        if (cachedBaseline == null) {
            MessageUtil.sendStat(sender, "Saved baseline", "none");
            MessageUtil.send(sender, MessageUtil.info("&7Create one when the server is healthy: &f/doctor baseline create&7."));
            MessageUtil.sendFooter(sender);
            return;
        }

        PerformanceBaseline baseline = cachedBaseline;
        MessageUtil.sendStat(sender, "Saved baseline", "yes");
        MessageUtil.sendStat(sender, "Created at", baseline.getCreatedAt());
        MessageUtil.sendStat(sender, "Created by", baseline.getCreatedBy());
        MessageUtil.blank(sender);
        MessageUtil.sendSection(sender, "Snapshot values");
        MessageUtil.sendStat(sender, "TPS", ServerStats.formatTps(baseline.getTps()));
        MessageUtil.sendStat(sender, "MSPT", ServerStats.formatMspt(baseline.getMspt()));
        MessageUtil.sendStat(sender, "Memory", String.format(Locale.ROOT, "%.1f%%", baseline.getMemoryUsagePercent()));
        MessageUtil.sendStat(sender, "Entities", String.format(Locale.ROOT, "%,d", baseline.getTotalEntities()));
        MessageUtil.sendStat(sender, "Loaded chunks", String.valueOf(baseline.getLoadedChunks()));
        MessageUtil.sendStat(sender, "Plugins", String.valueOf(baseline.getPluginCount()));
        MessageUtil.sendStat(sender, "Overall status", baseline.getOverallStatus());
        MessageUtil.sendFooter(sender);
    }

    public void compare(CommandSender sender) {
        BaselineConfig config = pluginConfig.getBaseline();
        if (!config.isEnabled()) {
            MessageUtil.send(sender, MessageUtil.error("&cBaseline system is disabled in config.yml."));
            return;
        }
        if (cachedBaseline == null) {
            MessageUtil.send(sender, MessageUtil.error(
                    "&cNo baseline saved. Run &f/doctor baseline create &cwhen the server is in a good state."
            ));
            return;
        }

        ServerStats current = ServerStats.collect(plugin.getEnabledAtMillis());
        BaselineComparison comparison = BaselineComparison.compare(current, cachedBaseline, config);

        MessageUtil.sendHeader(sender, "Baseline Compare");
        MessageUtil.sendStat(sender, "Baseline from", cachedBaseline.getCreatedAt());
        MessageUtil.sendStat(sender, "Baseline by", cachedBaseline.getCreatedBy());
        MessageUtil.blank(sender);
        MessageUtil.send(sender, MessageUtil.info(BaselineComparison.overallSummary(comparison.getOverallTrend())));
        MessageUtil.blank(sender);

        MessageUtil.sendSection(sender, "Changes vs baseline");
        sendComparisonLine(sender, "TPS", PerformanceBaseline.formatSignedChange(comparison.getTpsChange(), ""),
                comparison.getTpsTrend());
        sendComparisonLine(sender, "MSPT", PerformanceBaseline.formatSignedChange(comparison.getMsptChange(), "ms"),
                comparison.getMsptTrend());
        sendComparisonLine(sender, "Memory", PerformanceBaseline.formatSignedChange(comparison.getMemoryChange(), "%"),
                comparison.getMemoryTrend());
        sendComparisonLine(sender, "Entities", PerformanceBaseline.formatSignedLongChange(comparison.getEntityChange()),
                comparison.getEntityTrend());
        sendComparisonLine(sender, "Loaded chunks", PerformanceBaseline.formatSignedLongChange(comparison.getChunkChange()),
                comparison.getChunkTrend());
        sendComparisonLine(sender, "Plugins", PerformanceBaseline.formatSignedLongChange(comparison.getPluginChange()),
                comparison.getPluginTrend());

        MessageUtil.blank(sender);
        MessageUtil.sendSection(sender, "Current values");
        MessageUtil.sendStat(sender, "TPS", ServerStats.formatTps(current.currentTps));
        MessageUtil.sendStat(sender, "MSPT", ServerStats.formatMspt(current.mspt));
        MessageUtil.sendStat(sender, "Memory", String.format(Locale.ROOT, "%.1f%%", current.memoryUsagePercent));
        MessageUtil.sendStat(sender, "Entities", String.format(Locale.ROOT, "%,d", current.entityCount));
        MessageUtil.sendStat(sender, "Loaded chunks", String.valueOf(current.loadedChunkCount));

        if (!comparison.getRecommendations().isEmpty()) {
            MessageUtil.blank(sender);
            MessageUtil.sendSection(sender, "Suggestions");
            MessageUtil.send(sender, MessageUtil.info("&7Advisory only — does not identify an exact root cause."));
            for (String tip : comparison.getRecommendations()) {
                MessageUtil.send(sender, MessageUtil.recommendationBullet(tip));
            }
        }

        MessageUtil.sendFooter(sender);
    }

    public void requestDelete(CommandSender sender) {
        if (cachedBaseline == null) {
            MessageUtil.send(sender, MessageUtil.error("&cNo baseline saved to delete."));
            return;
        }
        MessageUtil.send(sender, MessageUtil.overallStatus(
                MessageUtil.StatusLevel.WARNING,
                "This will remove the saved baseline file."
        ));
        MessageUtil.send(sender, MessageUtil.info(
                "&7Confirm with &f/doctor baseline delete confirm&7 to permanently delete the baseline."
        ));
    }

    public void deleteConfirmed(CommandSender sender) {
        if (cachedBaseline == null) {
            MessageUtil.send(sender, MessageUtil.error("&cNo baseline saved to delete."));
            return;
        }

        if (!store.delete()) {
            MessageUtil.send(sender, MessageUtil.error("&cCould not delete baseline file. Check the server console."));
            return;
        }

        cachedBaseline = null;
        MessageUtil.send(sender, MessageUtil.info("&aBaseline deleted. Run &f/doctor baseline create &ato save a new one."));
        plugin.getLogger().info("Performance baseline deleted by " + sender.getName() + ".");
    }

    public void appendExportSection(StringBuilder report, ServerStats currentStats) {
        if (cachedBaseline == null) {
            return;
        }

        report.append(System.lineSeparator());
        report.append("=== Performance Baseline ===").append(System.lineSeparator());
        report.append(System.lineSeparator());
        report.append("Created at: ").append(cachedBaseline.getCreatedAt()).append(System.lineSeparator());
        report.append("Created by: ").append(cachedBaseline.getCreatedBy()).append(System.lineSeparator());
        report.append("Server version (at baseline): ").append(cachedBaseline.getServerVersion())
                .append(System.lineSeparator());
        report.append("Plugin version (at baseline): ").append(cachedBaseline.getPluginVersion())
                .append(System.lineSeparator());
        report.append("Baseline TPS: ").append(ServerStats.formatTps(cachedBaseline.getTps())).append(System.lineSeparator());
        report.append("Baseline MSPT: ").append(ServerStats.formatMspt(cachedBaseline.getMspt())).append(System.lineSeparator());
        report.append("Baseline memory: ").append(String.format(Locale.ROOT, "%.1f%%", cachedBaseline.getMemoryUsagePercent()))
                .append(System.lineSeparator());
        report.append("Baseline entities: ").append(cachedBaseline.getTotalEntities()).append(System.lineSeparator());
        report.append("Baseline loaded chunks: ").append(cachedBaseline.getLoadedChunks()).append(System.lineSeparator());
        report.append("Baseline plugin count: ").append(cachedBaseline.getPluginCount()).append(System.lineSeparator());
        report.append("Baseline overall status: ").append(cachedBaseline.getOverallStatus()).append(System.lineSeparator());

        BaselineComparison comparison = BaselineComparison.compare(
                currentStats,
                cachedBaseline,
                pluginConfig.getBaseline()
        );

        report.append(System.lineSeparator());
        report.append("=== Baseline Comparison (current vs saved) ===").append(System.lineSeparator());
        report.append(System.lineSeparator());
        report.append("Overall: ").append(comparison.getOverallTrend().label()).append(System.lineSeparator());
        report.append("TPS change: ").append(PerformanceBaseline.formatSignedChange(comparison.getTpsChange(), ""))
                .append(" (").append(comparison.getTpsTrend().label()).append(")").append(System.lineSeparator());
        report.append("MSPT change: ").append(PerformanceBaseline.formatSignedChange(comparison.getMsptChange(), "ms"))
                .append(" (").append(comparison.getMsptTrend().label()).append(")").append(System.lineSeparator());
        report.append("Memory change: ").append(PerformanceBaseline.formatSignedChange(comparison.getMemoryChange(), "%"))
                .append(" (").append(comparison.getMemoryTrend().label()).append(")").append(System.lineSeparator());
        report.append("Entity change: ").append(PerformanceBaseline.formatSignedLongChange(comparison.getEntityChange()))
                .append(" (").append(comparison.getEntityTrend().label()).append(")").append(System.lineSeparator());
        report.append("Loaded chunk change: ").append(PerformanceBaseline.formatSignedLongChange(comparison.getChunkChange()))
                .append(" (").append(comparison.getChunkTrend().label()).append(")").append(System.lineSeparator());
        report.append("Plugin count change: ").append(PerformanceBaseline.formatSignedLongChange(comparison.getPluginChange()))
                .append(" (").append(comparison.getPluginTrend().label()).append(")").append(System.lineSeparator());
        report.append(System.lineSeparator());
        report.append("Advisory suggestions:").append(System.lineSeparator());
        for (String tip : comparison.getRecommendations()) {
            report.append("• ").append(stripColorCodes(tip)).append(System.lineSeparator());
        }
    }

    private void sendComparisonLine(
            CommandSender sender,
            String label,
            String changeText,
            BaselineMetricTrend trend
    ) {
        MessageUtil.sendStat(
                sender,
                label,
                changeText + " (" + BaselineComparison.trendSummary(trend) + "&7)"
        );
    }

    private static String stripColorCodes(String text) {
        return text.replaceAll("&[0-9a-fk-or]", "");
    }
}
