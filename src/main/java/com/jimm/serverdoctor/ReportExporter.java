package com.jimm.serverdoctor;

import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Writes a read-only diagnostic .txt report for sharing with support or hosting providers.
 * Does not run cleanup or modify the world.
 */
public final class ReportExporter {

    private static final String REPORT_TITLE = "ServerDoctor Lite — Diagnostic Report";
    private static final DateTimeFormatter FILE_NAME_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
    private static final DateTimeFormatter REPORT_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ReportExporter() {
    }

    public static String export(
            ServerDoctorPlugin plugin,
            ServerStats stats,
            PluginConfig config,
            RecommendationService recommendationService,
            ChunkAnalyzerService chunkAnalyzerService
    ) throws IOException {
        PluginDataFolders.ensureReady(plugin);
        File reportsFolder = new File(plugin.getDataFolder(), "reports");

        String filename = "serverdoctor-diagnostic-" + LocalDateTime.now().format(FILE_NAME_TIME) + ".txt";
        File reportFile = new File(reportsFolder, filename);

        String content = buildReportText(plugin, stats, config, recommendationService, chunkAnalyzerService);
        Files.writeString(reportFile.toPath(), content, StandardCharsets.UTF_8);

        return filename;
    }

    private static String buildReportText(
            ServerDoctorPlugin plugin,
            ServerStats stats,
            PluginConfig config,
            RecommendationService recommendationService,
            ChunkAnalyzerService chunkAnalyzerService
    ) {
        StringBuilder report = new StringBuilder();

        appendReportHeader(report);
        appendServerInformation(report, plugin);
        appendPerformanceSummary(report, stats);
        appendEntityAndChunkSummary(report, stats);
        appendHeavyChunks(report, config, chunkAnalyzerService);
        appendLagSpikeSummary(report, plugin, config);
        appendCleanupSummary(report, plugin);
        appendWarnings(report, stats, config);
        appendRecommendations(report, stats, config, recommendationService);
        appendConfigThresholds(report, config);

        report.append(System.lineSeparator());
        report.append("End of diagnostic report.").append(System.lineSeparator());

        return report.toString();
    }

    private static void appendReportHeader(StringBuilder report) {
        report.append(REPORT_TITLE).append(System.lineSeparator());
        report.append("Generated: ").append(LocalDateTime.now().format(REPORT_TIME)).append(System.lineSeparator());
        report.append("Read-only export — no cleanup was run and no world changes were made.")
                .append(System.lineSeparator());
    }

    private static void appendServerInformation(StringBuilder report, ServerDoctorPlugin plugin) {
        appendSection(report, "Server Information");
        report.append("Server version: ").append(Bukkit.getVersion()).append(System.lineSeparator());
        report.append("Bukkit version: ").append(Bukkit.getBukkitVersion()).append(System.lineSeparator());
        report.append("Minecraft version: ").append(Bukkit.getMinecraftVersion()).append(System.lineSeparator());
        report.append("Paper / server implementation: ").append(Bukkit.getName()).append(System.lineSeparator());
        report.append("Java version: ").append(System.getProperty("java.version")).append(System.lineSeparator());
        report.append("ServerDoctor: ").append(PluginAbout.PLUGIN_NAME).append(" ").append(PluginAbout.EDITION)
                .append(" ").append(plugin.getPluginMeta().getVersion()).append(System.lineSeparator());
        report.append("Server name: ").append(Bukkit.getServer().getName()).append(System.lineSeparator());
    }

    private static void appendPerformanceSummary(StringBuilder report, ServerStats stats) {
        appendSection(report, "Performance Summary");
        report.append("TPS (1m average): ").append(ServerStats.formatTps(stats.currentTps)).append(System.lineSeparator());
        report.append("MSPT: ").append(ServerStats.formatMspt(stats.mspt)).append(System.lineSeparator());
        report.append("Memory usage: ").append(String.format("%.1f%%", stats.memoryUsagePercent))
                .append(" (").append(ServerStats.formatBytes(stats.usedBytes))
                .append(" / ").append(ServerStats.formatBytes(stats.maxBytes)).append(")")
                .append(System.lineSeparator());
        report.append("Online players: ").append(stats.onlinePlayers)
                .append(" / ").append(stats.maxPlayers).append(System.lineSeparator());
        report.append("Loaded worlds: ").append(stats.worldNames).append(System.lineSeparator());
        report.append("ServerDoctor plugin uptime: ").append(stats.uptime).append(System.lineSeparator());
    }

    private static void appendEntityAndChunkSummary(StringBuilder report, ServerStats stats) {
        appendSection(report, "Entity & Chunk Summary");
        report.append("Total entities: ").append(stats.entityCount).append(System.lineSeparator());
        report.append("Loaded chunks: ").append(stats.loadedChunkCount).append(System.lineSeparator());
        report.append("Dropped items (loaded): ").append(stats.droppedItemCount).append(System.lineSeparator());
        report.append("Mobs (non-player, loaded): ").append(stats.mobCount).append(System.lineSeparator());
        report.append("Hoppers (loaded chunks): ").append(stats.hopperCount).append(System.lineSeparator());
    }

    private static void appendHeavyChunks(
            StringBuilder report,
            PluginConfig config,
            ChunkAnalyzerService chunkAnalyzerService
    ) {
        appendSection(report, "Heavy Chunks");

        if (!config.isChunkAnalyzerEnabled()) {
            report.append("Chunk analyzer is disabled in config.yml.").append(System.lineSeparator());
            report.append("Enable chunk-analyzer-enabled to include heavy chunk data in exports.")
                    .append(System.lineSeparator());
            return;
        }

        List<ChunkAnalysisResult> allResults = chunkAnalyzerService.analyzeLoadedChunks();
        List<ChunkAnalysisResult> topResults = chunkAnalyzerService.getTopHeaviestChunks(allResults);

        report.append("Loaded chunks scanned: ").append(allResults.size()).append(System.lineSeparator());
        report.append("Top chunks listed: ").append(topResults.size()).append(System.lineSeparator());
        report.append(System.lineSeparator());

        if (topResults.isEmpty()) {
            report.append("No loaded chunks found to analyze.").append(System.lineSeparator());
            return;
        }

        int rank = 1;
        for (ChunkAnalysisResult result : topResults) {
            report.append("#").append(rank).append(" — ")
                    .append(result.worldName).append(" [")
                    .append(result.chunkCoordinates()).append("]")
                    .append(System.lineSeparator());
            report.append("  Heaviness score: ").append(String.format("%.2f", result.heavinessScore))
                    .append(System.lineSeparator());
            report.append("  Total entities: ").append(result.totalEntities).append(System.lineSeparator());
            report.append("  Dropped items: ").append(result.droppedItems).append(System.lineSeparator());
            report.append("  Mobs: ").append(result.mobs).append(System.lineSeparator());
            report.append("  Tile entities: ").append(result.tileEntities).append(System.lineSeparator());
            report.append("  Hoppers: ").append(result.hoppers).append(System.lineSeparator());
            report.append(System.lineSeparator());
            rank++;
        }
    }

    private static void appendLagSpikeSummary(StringBuilder report, ServerDoctorPlugin plugin, PluginConfig config) {
        appendSection(report, "Lag Spike Summary");

        LagSpikeConfig lagSpike = config.getLagSpike();
        report.append("Detection enabled: ").append(lagSpike.isEnabled()).append(System.lineSeparator());
        report.append("TPS drop threshold: ").append(lagSpike.getTpsDropThreshold()).append(System.lineSeparator());
        report.append("MSPT spike threshold: ").append(lagSpike.getMsptSpikeThreshold()).append(" ms")
                .append(System.lineSeparator());
        report.append(System.lineSeparator());

        if (!plugin.getLagSpikeHistory().hasLatestSpike()) {
            report.append("No lag spikes recorded since server start.").append(System.lineSeparator());
            return;
        }

        LagSpikeEvent latest = plugin.getLagSpikeHistory().getLatestSpike();
        report.append("Latest spike detected at: ").append(latest.formattedDetectedAt()).append(System.lineSeparator());
        report.append("Trigger: ").append(latest.triggerSummary()).append(System.lineSeparator());
        report.append("TPS at spike: ").append(ServerStats.formatTps(latest.tps())).append(System.lineSeparator());
        report.append("MSPT at spike: ").append(ServerStats.formatMspt(latest.mspt())).append(System.lineSeparator());
        report.append("Memory at spike: ").append(String.format("%.1f%%", latest.memoryUsagePercent()))
                .append(System.lineSeparator());
        report.append("Entities at spike: ").append(latest.entityCount()).append(System.lineSeparator());
        report.append("Loaded chunks at spike: ").append(latest.loadedChunkCount()).append(System.lineSeparator());
    }

    private static void appendCleanupSummary(StringBuilder report, ServerDoctorPlugin plugin) {
        appendSection(report, "Cleanup Summary");

        if (!plugin.getCleanupHistory().hasLastResult()) {
            report.append("No cleanup confirm has been run since server start.").append(System.lineSeparator());
            report.append("Use /doctor cleanup preview to scan — confirm only removes entities when you run it.")
                    .append(System.lineSeparator());
            return;
        }

        CleanupExecuteResult last = plugin.getCleanupHistory().getLastResult();
        report.append("Last cleanup at: ").append(last.formattedExecutedAt()).append(System.lineSeparator());
        report.append("Executor: ").append(last.executedBy()).append(System.lineSeparator());
        report.append("Worlds scanned: ").append(last.worldsScanned()).append(System.lineSeparator());
        report.append("Dropped items removed: ").append(last.droppedItemsRemoved()).append(System.lineSeparator());
        report.append("Hostile mobs removed: ").append(last.hostileMobsRemoved()).append(System.lineSeparator());
        report.append("Passive mobs removed: ").append(last.passiveMobsRemoved()).append(System.lineSeparator());
        report.append("Total removed: ").append(last.totalRemoved()).append(System.lineSeparator());
    }

    private static void appendWarnings(StringBuilder report, ServerStats stats, PluginConfig config) {
        appendSection(report, "Warnings");

        List<AlertType> activeAlerts = HealthChecker.findActiveAlerts(stats, config);
        if (activeAlerts.isEmpty()) {
            report.append("No major issues detected at export time.").append(System.lineSeparator());
            return;
        }

        for (AlertType type : activeAlerts) {
            report.append("- ").append(HealthChecker.formatReportWarning(type, stats, config))
                    .append(System.lineSeparator());
        }
    }

    private static void appendRecommendations(
            StringBuilder report,
            ServerStats stats,
            PluginConfig config,
            RecommendationService recommendationService
    ) {
        appendSection(report, "Recommendations");

        if (!recommendationService.isEnabled(config)) {
            report.append("Smart recommendations are disabled in config.yml.").append(System.lineSeparator());
            return;
        }

        List<String> tips = recommendationService.forHealthReport(stats, config);
        if (tips.isEmpty()) {
            report.append("No recommendations for current metrics.").append(System.lineSeparator());
            return;
        }

        for (String tip : tips) {
            report.append("• ").append(tip).append(System.lineSeparator());
        }
        report.append(System.lineSeparator());
        report.append("Advisory only — ServerDoctor does not change your world automatically.")
                .append(System.lineSeparator());
    }

    private static void appendConfigThresholds(StringBuilder report, PluginConfig config) {
        appendSection(report, "Config Thresholds");

        report.append("--- Health ---").append(System.lineSeparator());
        report.append("memory-warning-percent: ").append(config.getMemoryWarningPercent()).append(System.lineSeparator());
        report.append("entity-warning-limit: ").append(config.getEntityWarningLimit()).append(System.lineSeparator());
        report.append("loaded-chunk-warning-limit: ").append(config.getLoadedChunkWarningLimit()).append(System.lineSeparator());
        report.append("tps-warning-threshold: ").append(config.getTpsWarningThreshold()).append(System.lineSeparator());
        report.append("mspt-warning-threshold: ").append(config.getMsptWarningThreshold()).append(System.lineSeparator());
        report.append(System.lineSeparator());

        report.append("--- Alerts ---").append(System.lineSeparator());
        report.append("alerts-enabled: ").append(config.isAlertsEnabled()).append(System.lineSeparator());
        report.append("alert-check-interval-seconds: ").append(config.getAlertCheckIntervalSeconds()).append(System.lineSeparator());
        report.append("recommendations-enabled: ").append(config.isRecommendationsEnabled()).append(System.lineSeparator());
        report.append("discord-alerts-enabled: ").append(config.isDiscordAlertsEnabled()).append(System.lineSeparator());
        report.append("discord-webhook-url: ")
                .append(config.isDiscordWebhookConfigured() ? "configured (hidden)" : "not configured")
                .append(System.lineSeparator());
        report.append(System.lineSeparator());

        report.append("--- Chunk analyzer ---").append(System.lineSeparator());
        report.append("chunk-analyzer-enabled: ").append(config.isChunkAnalyzerEnabled()).append(System.lineSeparator());
        report.append("chunk-analyzer-top-limit: ").append(config.getChunkAnalyzerTopLimit()).append(System.lineSeparator());
        report.append("chunk-warning-entity-limit: ").append(config.getChunkWarningEntityLimit()).append(System.lineSeparator());
        report.append("chunk-warning-dropped-item-limit: ").append(config.getChunkWarningDroppedItemLimit())
                .append(System.lineSeparator());
        report.append("chunk-warning-hopper-limit: ").append(config.getChunkWarningHopperLimit()).append(System.lineSeparator());
        report.append("chunk-teleport-enabled: ").append(config.isChunkTeleportEnabled()).append(System.lineSeparator());
        report.append(System.lineSeparator());

        CleanupConfig cleanup = config.getCleanup();
        report.append("--- Cleanup ---").append(System.lineSeparator());
        report.append("cleanup.enabled: ").append(cleanup.isEnabled()).append(System.lineSeparator());
        report.append("cleanup.include-dropped-items: ").append(cleanup.isIncludeDroppedItems()).append(System.lineSeparator());
        report.append("cleanup.include-hostile-mobs: ").append(cleanup.isIncludeHostileMobs()).append(System.lineSeparator());
        report.append("cleanup.include-passive-mobs: ").append(cleanup.isIncludePassiveMobs()).append(System.lineSeparator());
        report.append("cleanup.cooldown-seconds: ").append(cleanup.getCooldownSeconds()).append(System.lineSeparator());
        report.append("cleanup.log-actions: ").append(cleanup.isLogActions()).append(System.lineSeparator());
        report.append(System.lineSeparator());

        LagSpikeConfig lagSpike = config.getLagSpike();
        report.append("--- Lag spike detection ---").append(System.lineSeparator());
        report.append("lag-spike-detection.enabled: ").append(lagSpike.isEnabled()).append(System.lineSeparator());
        report.append("lag-spike-detection.check-interval-seconds: ").append(lagSpike.getCheckIntervalSeconds())
                .append(System.lineSeparator());
        report.append("lag-spike-detection.tps-drop-threshold: ").append(lagSpike.getTpsDropThreshold())
                .append(System.lineSeparator());
        report.append("lag-spike-detection.mspt-spike-threshold: ").append(lagSpike.getMsptSpikeThreshold())
                .append(System.lineSeparator());
        report.append("lag-spike-detection.alert-cooldown-seconds: ").append(lagSpike.getAlertCooldownSeconds())
                .append(System.lineSeparator());
        report.append("lag-spike-detection.log-spikes: ").append(lagSpike.isLogSpikes()).append(System.lineSeparator());
    }

    private static void appendSection(StringBuilder report, String title) {
        report.append(System.lineSeparator());
        report.append("=== ").append(title).append(" ===").append(System.lineSeparator());
        report.append(System.lineSeparator());
    }
}
