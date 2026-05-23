package com.jimm.serverdoctor;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

public final class DoctorCommand implements BasicCommand {

    private final ServerDoctorPlugin plugin;
    private final long pluginEnabledAtMillis;
    private final PluginConfig pluginConfig;
    private final AlertService alertService;
    private final ChunkAnalyzerService chunkAnalyzerService;

    public DoctorCommand(
            ServerDoctorPlugin plugin,
            PluginConfig pluginConfig,
            AlertService alertService,
            ChunkAnalyzerService chunkAnalyzerService
    ) {
        this.plugin = plugin;
        this.pluginEnabledAtMillis = plugin.getEnabledAtMillis();
        this.pluginConfig = pluginConfig;
        this.alertService = alertService;
        this.chunkAnalyzerService = chunkAnalyzerService;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        CommandSender sender = source.getSender();

        if (args.length == 0) {
            if (!Permissions.canUse(sender)) {
                MessageUtil.send(sender, MessageUtil.permissionDenied(Permissions.USE));
                return;
            }
            sendQuickReport(sender, ServerStats.collect(pluginEnabledAtMillis));
            return;
        }

        String subcommand = args[0];

        if (subcommand.equalsIgnoreCase("report")) {
            if (!Permissions.canReport(sender)) {
                MessageUtil.send(sender, MessageUtil.permissionDenied(Permissions.REPORT));
                return;
            }
            sendHealthReport(sender, ServerStats.collect(pluginEnabledAtMillis));
            return;
        }

        if (subcommand.equalsIgnoreCase("reload")) {
            handleReload(sender);
            return;
        }

        if (subcommand.equalsIgnoreCase("alerts")) {
            if (!Permissions.canAlerts(sender)) {
                MessageUtil.send(sender, MessageUtil.permissionDenied(Permissions.ALERTS));
                return;
            }
            sendAlertsStatus(sender, ServerStats.collect(pluginEnabledAtMillis));
            return;
        }

        if (subcommand.equalsIgnoreCase("export")) {
            handleExport(sender);
            return;
        }

        if (subcommand.equalsIgnoreCase("discord")) {
            if (!Permissions.canAlerts(sender)) {
                MessageUtil.send(sender, MessageUtil.permissionDenied(Permissions.ALERTS));
                return;
            }
            sendDiscordStatus(sender);
            return;
        }

        if (subcommand.equalsIgnoreCase("chunks")) {
            handleChunks(sender);
            return;
        }

        if (subcommand.equalsIgnoreCase("help")) {
            if (!Permissions.canUse(sender)) {
                MessageUtil.send(sender, MessageUtil.permissionDenied(Permissions.USE));
                return;
            }
            sendHelp(sender);
            return;
        }

        MessageUtil.send(sender, MessageUtil.unknownCommand());
        sendHelp(sender);
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        CommandSender sender = source.getSender();
        if (args.length == 0 || args.length == 1) {
            String typed = args.length == 1 ? args[0].toLowerCase(Locale.ROOT) : "";
            return getPermittedSubcommands(sender).stream()
                    .filter(name -> name.startsWith(typed))
                    .toList();
        }
        return List.of();
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return Permissions.canRunAnyDoctorCommand(sender);
    }

    private void sendHelp(CommandSender sender) {
        MessageUtil.sendHeader(sender, "Help");
        boolean anyCommandListed = false;

        if (Permissions.canUse(sender)) {
            MessageUtil.sendSection(sender, "Commands");
            MessageUtil.send(sender, MessageUtil.helpEntry("/doctor", "Quick server stats"));
            anyCommandListed = true;
        }
        if (Permissions.canReport(sender)) {
            if (!anyCommandListed) {
                MessageUtil.sendSection(sender, "Commands");
            }
            MessageUtil.send(sender, MessageUtil.helpEntry("/doctor report", "Health report with warnings"));
            anyCommandListed = true;
        }
        if (Permissions.canAlerts(sender)) {
            if (!anyCommandListed) {
                MessageUtil.sendSection(sender, "Commands");
            }
            MessageUtil.send(sender, MessageUtil.helpEntry("/doctor alerts", "Automatic alert settings and status"));
            MessageUtil.send(sender, MessageUtil.helpEntry("/doctor discord", "Discord webhook alert status"));
            anyCommandListed = true;
        }
        if (Permissions.canExport(sender)) {
            if (!anyCommandListed) {
                MessageUtil.sendSection(sender, "Commands");
            }
            MessageUtil.send(sender, MessageUtil.helpEntry("/doctor export", "Save a text report to plugins/ServerDoctor/reports/"));
            anyCommandListed = true;
        }
        if (Permissions.canReload(sender)) {
            if (!anyCommandListed) {
                MessageUtil.sendSection(sender, "Commands");
            }
            MessageUtil.send(sender, MessageUtil.helpEntry("/doctor reload", "Reload config.yml without restarting"));
            anyCommandListed = true;
        }
        if (Permissions.canChunks(sender)) {
            if (!anyCommandListed) {
                MessageUtil.sendSection(sender, "Commands");
            }
            MessageUtil.send(sender, MessageUtil.helpEntry("/doctor chunks", "Find the heaviest loaded chunks"));
            anyCommandListed = true;
        }
        if (Permissions.canUse(sender)) {
            MessageUtil.send(sender, MessageUtil.helpEntry("/doctor help", "Show this help menu"));
        }
        if (!anyCommandListed) {
            MessageUtil.send(sender, MessageUtil.permissionDenied(Permissions.USE));
        }
        MessageUtil.sendFooter(sender);
    }

    private List<String> getPermittedSubcommands(CommandSender sender) {
        List<String> subcommands = new ArrayList<>();
        if (Permissions.canUse(sender)) {
            subcommands.add("help");
        }
        if (Permissions.canReport(sender)) {
            subcommands.add("report");
        }
        if (Permissions.canAlerts(sender)) {
            subcommands.add("alerts");
            subcommands.add("discord");
        }
        if (Permissions.canExport(sender)) {
            subcommands.add("export");
        }
        if (Permissions.canReload(sender)) {
            subcommands.add("reload");
        }
        if (Permissions.canChunks(sender)) {
            subcommands.add("chunks");
        }
        return subcommands;
    }

    private void handleChunks(CommandSender sender) {
        if (!Permissions.canChunks(sender)) {
            MessageUtil.send(sender, MessageUtil.permissionDenied(Permissions.CHUNKS));
            return;
        }

        if (!pluginConfig.isChunkAnalyzerEnabled()) {
            MessageUtil.send(sender, MessageUtil.error("&cChunk analyzer is disabled in config.yml."));
            return;
        }

        MessageUtil.send(sender, MessageUtil.info("&7Scanning loaded chunks on the main thread..."));

        plugin.getServer().getScheduler().runTask(plugin, () -> runChunkAnalysis(sender));
    }

    private void runChunkAnalysis(CommandSender sender) {
        List<ChunkAnalysisResult> allResults = chunkAnalyzerService.analyzeLoadedChunks();
        List<ChunkAnalysisResult> topResults = chunkAnalyzerService.getTopHeaviestChunks(allResults);

        MessageUtil.sendHeader(sender, "Chunk Analyzer");
        MessageUtil.sendStat(sender, "Loaded chunks scanned", String.valueOf(allResults.size()));
        MessageUtil.sendStat(sender, "Showing top", String.valueOf(topResults.size()));
        MessageUtil.blank(sender);

        if (topResults.isEmpty()) {
            MessageUtil.send(sender, MessageUtil.info("&7No loaded chunks found to analyze."));
            MessageUtil.sendFooter(sender);
            return;
        }

        int rank = 1;
        for (ChunkAnalysisResult result : topResults) {
            MessageUtil.sendSection(sender, "#" + rank + " — " + result.worldName + " [" + result.chunkCoordinates() + "]");
            MessageUtil.sendStat(sender, "Heaviness score", String.format("%.2f", result.heavinessScore));
            MessageUtil.sendStat(sender, "Total entities", String.valueOf(result.totalEntities));
            MessageUtil.sendStat(sender, "Dropped items", String.valueOf(result.droppedItems));
            MessageUtil.sendStat(sender, "Mobs", String.valueOf(result.mobs));
            MessageUtil.sendStat(sender, "Tile entities", String.valueOf(result.tileEntities));
            MessageUtil.sendStat(sender, "Hoppers", String.valueOf(result.hoppers));
            MessageUtil.send(sender, MessageUtil.section("Recommendations"));
            for (String tip : result.getRecommendations()) {
                MessageUtil.send(sender, MessageUtil.warningBullet(tip));
            }
            MessageUtil.blank(sender);
            rank++;
        }

        MessageUtil.sendFooter(sender);
    }

    private void handleExport(CommandSender sender) {
        if (!Permissions.canExport(sender)) {
            MessageUtil.send(sender, MessageUtil.permissionDenied(Permissions.EXPORT));
            return;
        }
        try {
            String filename = ReportExporter.export(
                    plugin,
                    ServerStats.collect(pluginEnabledAtMillis),
                    pluginConfig
            );
            MessageUtil.sendHeader(sender, "Export");
            MessageUtil.send(sender, MessageUtil.reportGenerated(filename));
            MessageUtil.sendFooter(sender);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "Failed to export ServerDoctor report", exception);
            MessageUtil.send(sender, MessageUtil.error("&cFailed to generate report. Check the server console for details."));
        }
    }

    private void handleReload(CommandSender sender) {
        if (!Permissions.canReload(sender)) {
            MessageUtil.send(sender, MessageUtil.permissionDenied(Permissions.RELOAD));
            return;
        }
        pluginConfig.load();
        alertService.start();
        MessageUtil.send(sender, MessageUtil.reloadSuccess());
    }

    private void sendDiscordStatus(CommandSender sender) {
        MessageUtil.sendHeader(sender, "Discord Alerts");
        MessageUtil.sendSection(sender, "Configuration");
        MessageUtil.sendStat(sender, "Discord alerts enabled", pluginConfig.isDiscordAlertsEnabled() ? "yes" : "no");
        MessageUtil.sendStat(
                sender,
                "Webhook URL",
                pluginConfig.isDiscordWebhookConfigured() ? "configured (hidden for security)" : "not configured"
        );
        MessageUtil.sendStat(sender, "Embed title", pluginConfig.getDiscordAlertTitle());
        MessageUtil.blank(sender);

        if (pluginConfig.shouldSendDiscordAlerts()) {
            MessageUtil.send(sender, MessageUtil.info("&7Discord will receive alerts when in-game alerts fire (same cooldown)."));
        } else if (pluginConfig.isDiscordAlertsEnabled()) {
            MessageUtil.send(sender, MessageUtil.error("&cDiscord is enabled but webhook URL is empty. Add a URL in config.yml."));
        } else {
            MessageUtil.send(sender, MessageUtil.info("&7Enable discord-alerts-enabled and set a webhook URL in config.yml."));
        }

        MessageUtil.sendFooter(sender);
    }

    private void sendAlertsStatus(CommandSender sender, ServerStats stats) {
        List<AlertType> active = HealthChecker.findActiveAlerts(stats, pluginConfig);
        MessageUtil.StatusLevel overall = MessageUtil.overallStatusFromAlerts(active);

        MessageUtil.sendHeader(sender, "Alerts");
        MessageUtil.sendSection(sender, "Settings");
        MessageUtil.sendStat(sender, "Alerts enabled", pluginConfig.isAlertsEnabled() ? "yes" : "no");
        MessageUtil.sendStat(sender, "Check interval", pluginConfig.getAlertCheckIntervalSeconds() + " seconds");
        MessageUtil.sendStat(sender, "Alert cooldown", AlertService.getAlertCooldownMinutes() + " minutes per warning");
        MessageUtil.sendStat(sender, "Notify permission", Permissions.ALERTS);
        MessageUtil.blank(sender);

        MessageUtil.sendSection(sender, "Live metrics");
        sendMetricStat(sender, AlertType.MEMORY, stats);
        sendMetricStat(sender, AlertType.ENTITIES, stats);
        sendMetricStat(sender, AlertType.CHUNKS, stats);
        sendMetricStat(sender, AlertType.TPS, stats);
        sendMetricStat(sender, AlertType.MSPT, stats);
        MessageUtil.blank(sender);

        MessageUtil.sendSection(sender, "Summary");
        if (active.isEmpty()) {
            MessageUtil.send(sender, MessageUtil.overallStatus(overall, "All metrics are within configured limits."));
        } else {
            MessageUtil.send(sender, MessageUtil.overallStatus(
                    overall,
                    active.size() + " threshold(s) exceeded — subscribed players may receive alerts."
            ));
        }
        MessageUtil.sendFooter(sender);
    }

    private void sendMetricStat(CommandSender sender, AlertType type, ServerStats stats) {
        MessageUtil.sendStat(
                sender,
                metricLabel(type),
                MessageUtil.formatMetricValue(type, stats, pluginConfig),
                MessageUtil.metricStatus(type, stats, pluginConfig)
        );
    }

    private static String metricLabel(AlertType type) {
        return switch (type) {
            case MEMORY -> "Memory";
            case ENTITIES -> "Entities";
            case CHUNKS -> "Loaded chunks";
            case TPS -> "TPS";
            case MSPT -> "MSPT";
        };
    }

    private void sendQuickReport(CommandSender sender, ServerStats stats) {
        MessageUtil.sendHeader(sender, "Quick Report");
        MessageUtil.sendSection(sender, "Server snapshot");
        MessageUtil.sendStat(sender, "Players", stats.onlinePlayers + " / " + stats.maxPlayers);
        MessageUtil.sendStat(
                sender,
                "Memory",
                ServerStats.formatBytes(stats.usedBytes) + " / " + ServerStats.formatBytes(stats.maxBytes)
        );
        MessageUtil.sendStat(sender, "Worlds", stats.worldNames);
        MessageUtil.sendStat(sender, "Entities", String.valueOf(stats.entityCount));
        MessageUtil.sendStat(sender, "TPS", ServerStats.formatTps(stats.currentTps));
        MessageUtil.sendStat(sender, "MSPT", ServerStats.formatMspt(stats.mspt));
        MessageUtil.sendStat(sender, "Plugin uptime", stats.uptime);
        MessageUtil.sendFooter(sender);
    }

    private void sendHealthReport(CommandSender sender, ServerStats stats) {
        List<String> warnings = buildWarnings(stats);
        MessageUtil.StatusLevel overall = MessageUtil.overallStatusFromAlerts(
                HealthChecker.findActiveAlerts(stats, pluginConfig)
        );

        MessageUtil.sendHeader(sender, "Health Report");
        MessageUtil.sendSection(sender, "Server snapshot");
        MessageUtil.sendStat(sender, "Players", stats.onlinePlayers + " / " + stats.maxPlayers);
        MessageUtil.sendStat(
                sender,
                "Memory",
                String.format("%.1f%%", stats.memoryUsagePercent)
                        + " (" + ServerStats.formatBytes(stats.usedBytes) + " / " + ServerStats.formatBytes(stats.maxBytes) + ")"
        );
        MessageUtil.sendStat(sender, "Worlds", stats.worldNames);
        MessageUtil.sendStat(sender, "Entities", String.valueOf(stats.entityCount));
        MessageUtil.sendStat(sender, "Loaded chunks", String.valueOf(stats.loadedChunkCount));
        MessageUtil.sendStat(sender, "TPS", ServerStats.formatTps(stats.currentTps));
        MessageUtil.sendStat(sender, "MSPT", ServerStats.formatMspt(stats.mspt));
        MessageUtil.sendStat(sender, "Plugin uptime", stats.uptime);
        MessageUtil.blank(sender);

        MessageUtil.sendSection(sender, "Diagnostics");
        if (warnings.isEmpty()) {
            MessageUtil.send(sender, MessageUtil.overallStatus(
                    overall,
                    pluginConfig.getMessages().noWarningsText()
            ));
        } else {
            MessageUtil.send(sender, MessageUtil.overallStatus(
                    overall,
                    warnings.size() + " issue(s) found — see warnings below."
            ));
            MessageUtil.blank(sender);
            MessageUtil.sendSection(sender, "Warnings");
            for (String warning : warnings) {
                MessageUtil.send(sender, MessageUtil.warningBullet(warning));
            }
        }
        MessageUtil.sendFooter(sender);
    }

    private List<String> buildWarnings(ServerStats stats) {
        List<String> warnings = new ArrayList<>();
        for (AlertType type : HealthChecker.findActiveAlerts(stats, pluginConfig)) {
            warnings.add(HealthChecker.formatReportWarning(type, stats, pluginConfig));
        }
        return warnings;
    }
}
