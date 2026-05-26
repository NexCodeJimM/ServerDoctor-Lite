package com.jimm.serverdoctor;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.OptionalLong;
import java.util.logging.Level;

public final class DoctorCommand implements CommandExecutor, TabCompleter {

    private final ServerDoctorPlugin plugin;
    private final long pluginEnabledAtMillis;
    private final PluginConfig pluginConfig;
    private final AlertService alertService;
    private final ChunkAnalyzerService chunkAnalyzerService;
    private final RecommendationService recommendationService;
    private final LagSpikeDetectorService lagSpikeDetectorService;
    private final UpdateCheckerService updateCheckerService;
    private final PluginImpactScannerService pluginImpactScannerService;
    private final PerformanceHistoryService performanceHistoryService;
    private final ScheduledReportService scheduledReportService;

    public DoctorCommand(
            ServerDoctorPlugin plugin,
            PluginConfig pluginConfig,
            AlertService alertService,
            ChunkAnalyzerService chunkAnalyzerService,
            RecommendationService recommendationService,
            LagSpikeDetectorService lagSpikeDetectorService,
            UpdateCheckerService updateCheckerService,
            PluginImpactScannerService pluginImpactScannerService,
            PerformanceHistoryService performanceHistoryService,
            ScheduledReportService scheduledReportService
    ) {
        this.plugin = plugin;
        this.pluginEnabledAtMillis = plugin.getEnabledAtMillis();
        this.pluginConfig = pluginConfig;
        this.alertService = alertService;
        this.chunkAnalyzerService = chunkAnalyzerService;
        this.recommendationService = recommendationService;
        this.lagSpikeDetectorService = lagSpikeDetectorService;
        this.updateCheckerService = updateCheckerService;
        this.pluginImpactScannerService = pluginImpactScannerService;
        this.performanceHistoryService = performanceHistoryService;
        this.scheduledReportService = scheduledReportService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!Permissions.canRunAnyDoctorCommand(sender)) {
            MessageUtil.send(sender, MessageUtil.permissionDenied(Permissions.USE));
            return true;
        }

        if (args.length == 0) {
            if (!Permissions.canUse(sender)) {
                MessageUtil.send(sender, MessageUtil.permissionDenied(Permissions.USE));
                return true;
            }
            sendQuickReport(sender, ServerStats.collect(pluginEnabledAtMillis));
            return true;
        }

        String subcommand = args[0];

        if (subcommand.equalsIgnoreCase("report")) {
            if (!Permissions.canReport(sender)) {
                MessageUtil.send(sender, MessageUtil.permissionDenied(Permissions.REPORT));
                return true;
            }
            sendHealthReport(sender, ServerStats.collect(pluginEnabledAtMillis));
            return true;
        }

        if (subcommand.equalsIgnoreCase("reload")) {
            handleReload(sender);
            return true;
        }

        if (subcommand.equalsIgnoreCase("alerts")) {
            if (!Permissions.canAlerts(sender)) {
                MessageUtil.send(sender, MessageUtil.permissionDenied(Permissions.ALERTS));
                return true;
            }
            sendAlertsStatus(sender, ServerStats.collect(pluginEnabledAtMillis));
            return true;
        }

        if (subcommand.equalsIgnoreCase("export")) {
            handleExport(sender);
            return true;
        }

        if (subcommand.equalsIgnoreCase("discord")) {
            if (!Permissions.canAlerts(sender)) {
                MessageUtil.send(sender, MessageUtil.permissionDenied(Permissions.ALERTS));
                return true;
            }
            sendDiscordStatus(sender);
            return true;
        }

        if (subcommand.equalsIgnoreCase("chunks")) {
            handleChunks(sender);
            return true;
        }

        if (subcommand.equalsIgnoreCase("tpchunk")) {
            handleTpChunk(sender, args);
            return true;
        }

        if (subcommand.equalsIgnoreCase("cleanup")) {
            handleCleanup(sender, args);
            return true;
        }

        if (subcommand.equalsIgnoreCase("spikes")) {
            if (!Permissions.canSpikes(sender)) {
                MessageUtil.send(sender, MessageUtil.permissionDenied(Permissions.SPIKES));
                return true;
            }
            sendSpikesStatus(sender);
            return true;
        }

        if (subcommand.equalsIgnoreCase("status")) {
            if (!Permissions.canStatus(sender)) {
                MessageUtil.send(sender, MessageUtil.permissionDenied(Permissions.STATUS));
                return true;
            }
            sendServerStatus(sender, ServerStats.collect(pluginEnabledAtMillis));
            return true;
        }

        if (subcommand.equalsIgnoreCase("about")) {
            if (!Permissions.canAbout(sender)) {
                MessageUtil.send(sender, MessageUtil.permissionDenied(Permissions.ABOUT));
                return true;
            }
            PluginAbout.sendAbout(sender, plugin);
            return true;
        }

        if (subcommand.equalsIgnoreCase("update")) {
            handleUpdate(sender, args);
            return true;
        }

        if (subcommand.equalsIgnoreCase("plugins")) {
            handlePlugins(sender);
            return true;
        }

        if (subcommand.equalsIgnoreCase("history")) {
            handleHistory(sender, args);
            return true;
        }

        if (subcommand.equalsIgnoreCase("schedule")) {
            if (!Permissions.canSchedule(sender)) {
                MessageUtil.send(sender, MessageUtil.permissionDenied(Permissions.SCHEDULE));
                return true;
            }
            scheduledReportService.sendStatus(sender);
            return true;
        }

        if (subcommand.equalsIgnoreCase("help")) {
            if (!Permissions.canUse(sender)) {
                MessageUtil.send(sender, MessageUtil.permissionDenied(Permissions.USE));
                return true;
            }
            sendHelp(sender);
            return true;
        }

        MessageUtil.send(sender, MessageUtil.unknownCommand());
        sendHelp(sender);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>(suggestSubcommands(sender, args));
    }

    private Collection<String> suggestSubcommands(CommandSender sender, String[] args) {

        if (args.length == 0 || args.length == 1) {
            String typed = args.length == 1 ? args[0].toLowerCase(Locale.ROOT) : "";
            return getPermittedSubcommands(sender).stream()
                    .filter(name -> name.startsWith(typed))
                    .toList();
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("tpchunk") && Permissions.canTpChunk(sender)) {
            if (args.length == 2) {
                String typedWorld = args[1].toLowerCase(Locale.ROOT);
                return Bukkit.getWorlds().stream()
                        .map(World::getName)
                        .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(typedWorld))
                        .toList();
            }
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("cleanup")
                && (Permissions.canCleanupPreview(sender) || Permissions.canCleanupConfirm(sender))) {
            if (args.length == 2) {
                String typed = args[1].toLowerCase(Locale.ROOT);
                return cleanupSubcommandSuggestions(sender).stream()
                        .filter(option -> option.startsWith(typed))
                        .toList();
            }
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("update") && Permissions.canUpdateCheck(sender)) {
            if (args.length == 2) {
                String typed = args[1].toLowerCase(Locale.ROOT);
                if ("check".startsWith(typed)) {
                    return List.of("check");
                }
            }
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("history")) {
            if (args.length == 2) {
                String typed = args[1].toLowerCase(Locale.ROOT);
                return historySubcommandSuggestions(sender).stream()
                        .filter(option -> option.startsWith(typed))
                        .toList();
            }
        }

        return List.of();
    }

    private void sendHelp(CommandSender sender) {
        MessageUtil.sendHeader(sender, "Help");
        boolean anyCommandListed = false;

        if (Permissions.canAbout(sender)) {
            MessageUtil.sendSection(sender, "Commands");
            MessageUtil.send(sender, MessageUtil.helpEntry("/doctor about", "Plugin version and information"));
            anyCommandListed = true;
        }
        if (Permissions.canUse(sender)) {
            if (!anyCommandListed) {
                MessageUtil.sendSection(sender, "Commands");
            }
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
        if (Permissions.canTpChunk(sender)) {
            if (!anyCommandListed) {
                MessageUtil.sendSection(sender, "Commands");
            }
            MessageUtil.send(sender, MessageUtil.helpEntry(
                    "/doctor tpchunk <world> <chunkX> <chunkZ>",
                    "Teleport to the center of a chunk (also clickable in /doctor chunks)"
            ));
            anyCommandListed = true;
        }
        if (Permissions.canCleanupPreview(sender)) {
            if (!anyCommandListed) {
                MessageUtil.sendSection(sender, "Commands");
            }
            MessageUtil.send(sender, MessageUtil.helpEntry(
                    "/doctor cleanup preview",
                    "Preview entity counts — nothing is removed"
            ));
            anyCommandListed = true;
        }
        if (Permissions.canCleanupConfirm(sender)) {
            if (!anyCommandListed) {
                MessageUtil.sendSection(sender, "Commands");
            }
            MessageUtil.send(sender, MessageUtil.helpEntry(
                    "/doctor cleanup confirm",
                    "Remove configured entities from loaded worlds (run preview first)"
            ));
            anyCommandListed = true;
        }
        if (Permissions.canSpikes(sender)) {
            if (!anyCommandListed) {
                MessageUtil.sendSection(sender, "Commands");
            }
            MessageUtil.send(sender, MessageUtil.helpEntry(
                    "/doctor spikes",
                    "Lag spike detection status and latest spike summary"
            ));
            anyCommandListed = true;
        }
        if (Permissions.canStatus(sender)) {
            if (!anyCommandListed) {
                MessageUtil.sendSection(sender, "Commands");
            }
            MessageUtil.send(sender, MessageUtil.helpEntry(
                    "/doctor status",
                    "Quick overall server health summary (GOOD / WARNING / CRITICAL)"
            ));
            anyCommandListed = true;
        }
        if (Permissions.canUpdateNotify(sender) || Permissions.canUpdateCheck(sender)) {
            if (!anyCommandListed) {
                MessageUtil.sendSection(sender, "Commands");
            }
            MessageUtil.send(sender, MessageUtil.helpEntry(
                    "/doctor update",
                    "SpigotMC version status and download link"
            ));
            if (Permissions.canUpdateCheck(sender)) {
                MessageUtil.send(sender, MessageUtil.helpEntry(
                        "/doctor update check",
                        "Check SpigotMC now for a newer version"
                ));
            }
            anyCommandListed = true;
        }
        if (Permissions.canPlugins(sender)) {
            if (!anyCommandListed) {
                MessageUtil.sendSection(sender, "Commands");
            }
            MessageUtil.send(sender, MessageUtil.helpEntry(
                    "/doctor plugins",
                    "Scan installed plugins for areas worth performance review"
            ));
            anyCommandListed = true;
        }
        if (Permissions.canHistory(sender) || Permissions.canHistorySpikes(sender)
                || Permissions.canHistoryPerformance(sender)) {
            if (!anyCommandListed) {
                MessageUtil.sendSection(sender, "Commands");
            }
            if (Permissions.canHistory(sender)) {
                MessageUtil.send(sender, MessageUtil.helpEntry(
                        "/doctor history",
                        "Performance history overview and trend summary"
                ));
            }
            if (Permissions.canHistorySpikes(sender)) {
                MessageUtil.send(sender, MessageUtil.helpEntry(
                        "/doctor history spikes",
                        "Recent lag spikes with timestamps and severity"
                ));
            }
            if (Permissions.canHistoryPerformance(sender)) {
                MessageUtil.send(sender, MessageUtil.helpEntry(
                        "/doctor history performance",
                        "Rolling averages and improved/degraded trends"
                ));
            }
            anyCommandListed = true;
        }
        if (Permissions.canSchedule(sender)) {
            if (!anyCommandListed) {
                MessageUtil.sendSection(sender, "Commands");
            }
            MessageUtil.send(sender, MessageUtil.helpEntry(
                    "/doctor schedule",
                    "Scheduled diagnostic report status and next run time"
            ));
            anyCommandListed = true;
        }
        if (Permissions.canUse(sender)) {
            MessageUtil.send(sender, MessageUtil.helpEntry("/doctor help", "Show this help menu"));
        }
        if (!anyCommandListed) {
            MessageUtil.send(sender, MessageUtil.error("&cYou do not have permission to use any /doctor commands."));
        }
        MessageUtil.sendFooter(sender);
    }

    private List<String> getPermittedSubcommands(CommandSender sender) {
        List<String> subcommands = new ArrayList<>();
        if (Permissions.canAbout(sender)) {
            subcommands.add("about");
        }
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
        if (Permissions.canTpChunk(sender)) {
            subcommands.add("tpchunk");
        }
        if (Permissions.canCleanupPreview(sender) || Permissions.canCleanupConfirm(sender)) {
            subcommands.add("cleanup");
        }
        if (Permissions.canSpikes(sender)) {
            subcommands.add("spikes");
        }
        if (Permissions.canStatus(sender)) {
            subcommands.add("status");
        }
        if (Permissions.canUpdateNotify(sender) || Permissions.canUpdateCheck(sender)) {
            subcommands.add("update");
        }
        if (Permissions.canPlugins(sender)) {
            subcommands.add("plugins");
        }
        if (Permissions.canHistory(sender) || Permissions.canHistorySpikes(sender)
                || Permissions.canHistoryPerformance(sender)) {
            subcommands.add("history");
        }
        if (Permissions.canSchedule(sender)) {
            subcommands.add("schedule");
        }
        return subcommands;
    }

    private List<String> historySubcommandSuggestions(CommandSender sender) {
        List<String> options = new ArrayList<>();
        if (Permissions.canHistorySpikes(sender)) {
            options.add("spikes");
        }
        if (Permissions.canHistoryPerformance(sender)) {
            options.add("performance");
        }
        return options;
    }

    private void handleHistory(CommandSender sender, String[] args) {
        if (args.length >= 2) {
            String sub = args[1];
            if (sub.equalsIgnoreCase("spikes")) {
                if (!Permissions.canHistorySpikes(sender)) {
                    MessageUtil.send(sender, MessageUtil.permissionDenied(Permissions.HISTORY_SPIKES));
                    return;
                }
                performanceHistoryService.sendSpikeHistory(sender);
                return;
            }
            if (sub.equalsIgnoreCase("performance")) {
                if (!Permissions.canHistoryPerformance(sender)) {
                    MessageUtil.send(sender, MessageUtil.permissionDenied(Permissions.HISTORY_PERFORMANCE));
                    return;
                }
                performanceHistoryService.sendPerformanceHistory(sender);
                return;
            }
        }

        if (!Permissions.canHistory(sender)) {
            MessageUtil.send(sender, MessageUtil.permissionDenied(Permissions.HISTORY));
            return;
        }
        performanceHistoryService.sendOverview(sender);
    }

    private void handlePlugins(CommandSender sender) {
        if (!Permissions.canPlugins(sender)) {
            MessageUtil.send(sender, MessageUtil.permissionDenied(Permissions.PLUGINS));
            return;
        }

        PluginImpactScannerConfig scannerConfig = pluginConfig.getPluginImpactScanner();
        if (!scannerConfig.isEnabled()) {
            MessageUtil.send(sender, MessageUtil.error("&cPlugin impact scanner is disabled in config.yml."));
            return;
        }

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            PluginImpactScanResult result = pluginImpactScannerService.scan(pluginConfig);
            pluginImpactScannerService.sendReport(sender, pluginConfig, result, scannerConfig);
        });
    }

    private void handleUpdate(CommandSender sender, String[] args) {
        if (args.length >= 2 && args[1].equalsIgnoreCase("check")) {
            if (!Permissions.canUpdateCheck(sender)) {
                MessageUtil.send(sender, MessageUtil.permissionDenied(Permissions.UPDATE_CHECK));
                return;
            }
            MessageUtil.send(sender, MessageUtil.info("&7Checking SpigotMC for updates..."));
            updateCheckerService.runCheckAsync(true, sender);
            return;
        }

        if (!Permissions.canUpdateNotify(sender)) {
            MessageUtil.send(sender, MessageUtil.permissionDenied(Permissions.UPDATE_NOTIFY));
            return;
        }
        updateCheckerService.sendUpdateStatus(sender, false);
    }

    private void sendServerStatus(CommandSender sender, ServerStats stats) {
        ServerHealthStatus health = ServerHealthStatusService.evaluate(
                stats,
                pluginConfig,
                plugin.getLagSpikeHistory(),
                recommendationService
        );

        MessageUtil.sendHeader(sender, "Server Status");
        MessageUtil.send(sender, MessageUtil.overallStatus(health.overall(), health.summaryMessage()));
        MessageUtil.blank(sender);

        MessageUtil.sendSection(sender, "Metrics");
        sendStatusMetric(sender, "TPS", ServerStats.formatTps(stats.currentTps), health.tps());
        sendStatusMetric(sender, "MSPT", ServerStats.formatMspt(stats.mspt), health.mspt());
        sendStatusMetric(
                sender,
                "Memory",
                String.format(Locale.ROOT, "%.1f%%", stats.memoryUsagePercent),
                health.memory()
        );
        sendStatusMetric(sender, "Entities", String.format(Locale.ROOT, "%,d", stats.entityCount), health.entities());
        sendStatusMetric(sender, "Loaded chunks", String.valueOf(stats.loadedChunkCount), health.chunks());
        MessageUtil.blank(sender);

        MessageUtil.sendSection(sender, "Lag spike");
        MessageUtil.sendStat(sender, "Status", health.lagSpike().label(), health.lagSpike());
        MessageUtil.sendStat(sender, "Latest", health.latestLagSpikeLine());
        MessageUtil.blank(sender);

        MessageUtil.sendSection(sender, "Recommendations");
        if (pluginConfig.isRecommendationsEnabled()) {
            MessageUtil.sendStat(sender, "Active tips", String.valueOf(health.recommendationCount()));
            if (health.recommendationCount() > 0) {
                MessageUtil.send(sender, MessageUtil.info("&7Run &f/doctor report &7to see full recommendations."));
            }
        } else {
            MessageUtil.sendStat(sender, "Active tips", "disabled in config");
        }

        MessageUtil.sendFooter(sender);
    }

    private void sendStatusMetric(
            CommandSender sender,
            String label,
            String value,
            MessageUtil.StatusLevel level
    ) {
        MessageUtil.sendStat(sender, label, value, level);
    }

    private List<String> cleanupSubcommandSuggestions(CommandSender sender) {
        List<String> options = new ArrayList<>();
        if (Permissions.canCleanupPreview(sender)) {
            options.add("preview");
        }
        if (Permissions.canCleanupConfirm(sender)) {
            options.add("confirm");
        }
        return options;
    }

    private void handleCleanup(CommandSender sender, String[] args) {
        if (!Permissions.canCleanupPreview(sender) && !Permissions.canCleanupConfirm(sender)) {
            MessageUtil.send(sender, MessageUtil.permissionDenied(Permissions.CLEANUP_PREVIEW));
            return;
        }

        if (args.length < 2) {
            MessageUtil.send(sender, MessageUtil.error("&cUsage: /doctor cleanup <preview|confirm>"));
            return;
        }

        if (args[1].equalsIgnoreCase("preview")) {
            handleCleanupPreview(sender);
            return;
        }

        if (args[1].equalsIgnoreCase("confirm")) {
            handleCleanupConfirm(sender);
            return;
        }

        MessageUtil.send(sender, MessageUtil.error("&cUsage: /doctor cleanup <preview|confirm>"));
    }

    private void handleCleanupPreview(CommandSender sender) {
        if (!Permissions.canCleanupPreview(sender)) {
            MessageUtil.send(sender, MessageUtil.permissionDenied(Permissions.CLEANUP_PREVIEW));
            return;
        }

        if (!pluginConfig.getCleanup().isEnabled()) {
            MessageUtil.send(sender, MessageUtil.error("&cCleanup is disabled in config.yml."));
            return;
        }

        MessageUtil.send(sender, MessageUtil.info("&7Scanning loaded worlds on the main thread..."));
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (!isSenderAvailable(sender)) {
                return;
            }
            sendCleanupPreview(sender);
        });
    }

    private void sendCleanupPreview(CommandSender sender) {
        CleanupPreviewResult result = CleanupPreviewService.scanLoadedWorlds();
        CleanupConfig cleanup = pluginConfig.getCleanup();
        int eligibleToRemove = cleanup.countEligibleToRemove();

        MessageUtil.sendHeader(sender, "Cleanup Preview");
        MessageUtil.sendSection(sender, "Scan");
        MessageUtil.sendStat(sender, "Worlds scanned", String.valueOf(result.worldsScanned()));
        MessageUtil.blank(sender);

        MessageUtil.sendSection(sender, "Entity counts (loaded worlds)");
        MessageUtil.sendStat(sender, "Dropped items", String.valueOf(result.droppedItems()));
        MessageUtil.sendStat(sender, "Hostile mobs", String.valueOf(result.hostileMobs()));
        MessageUtil.sendStat(sender, "Passive mobs", String.valueOf(result.passiveMobs()));
        MessageUtil.sendStat(sender, "Total living entities", String.valueOf(result.totalLivingEntities()));
        MessageUtil.sendStat(sender, "Players (never removed)", String.valueOf(result.players()));
        MessageUtil.blank(sender);

        MessageUtil.sendSection(sender, "Cleanup config");
        MessageUtil.sendStat(sender, "Include dropped items", yesNo(cleanup.isIncludeDroppedItems()));
        MessageUtil.sendStat(sender, "Include hostile mobs", yesNo(cleanup.isIncludeHostileMobs()));
        MessageUtil.sendStat(sender, "Include passive mobs", yesNo(cleanup.isIncludePassiveMobs()));
        MessageUtil.sendStat(sender, "Eligible to remove (safe rules)", String.valueOf(eligibleToRemove));
        sendLatestCleanupSummary(sender, cleanup);
        MessageUtil.blank(sender);

        MessageUtil.send(sender, MessageUtil.overallStatus(
                MessageUtil.StatusLevel.GOOD,
                "Preview only. No entities were removed."
        ));
        if (Permissions.canCleanupConfirm(sender)) {
            MessageUtil.send(sender, MessageUtil.info("&7To execute cleanup, run &f/doctor cleanup confirm&7."));
        }
        MessageUtil.sendFooter(sender);
    }

    private void handleCleanupConfirm(CommandSender sender) {
        if (!Permissions.canCleanupConfirm(sender)) {
            MessageUtil.send(sender, MessageUtil.permissionDenied(Permissions.CLEANUP_CONFIRM));
            return;
        }

        if (!pluginConfig.getCleanup().isEnabled()) {
            MessageUtil.send(sender, MessageUtil.error("&cCleanup is disabled in config.yml."));
            return;
        }

        CleanupConfig cleanup = pluginConfig.getCleanup();
        if (!CleanupExecuteService.hasAnyCategoryEnabled(cleanup)) {
            MessageUtil.send(sender, MessageUtil.error("&cNo cleanup categories are enabled in config.yml."));
            return;
        }

        OptionalLong cooldownRemaining = CleanupCooldownService.remainingSeconds(
                plugin.getCleanupHistory(),
                cleanup.getCooldownSeconds()
        );
        if (cooldownRemaining.isPresent()) {
            MessageUtil.send(sender, MessageUtil.error(String.format(
                    Locale.ROOT,
                    "&cCleanup is on cooldown. Try again in &f%s&c.",
                    CleanupCooldownService.formatRemaining(cooldownRemaining.getAsLong())
            )));
            return;
        }

        MessageUtil.sendHeader(sender, "Cleanup Confirm");
        MessageUtil.send(sender, MessageUtil.overallStatus(
                MessageUtil.StatusLevel.WARNING,
                "This will remove configured entities from loaded worlds."
        ));
        MessageUtil.sendStat(sender, "Include dropped items", yesNo(cleanup.isIncludeDroppedItems()));
        MessageUtil.sendStat(sender, "Include hostile mobs", yesNo(cleanup.isIncludeHostileMobs()));
        MessageUtil.sendStat(sender, "Include passive mobs", yesNo(cleanup.isIncludePassiveMobs()));
        MessageUtil.sendStat(sender, "Eligible to remove now", String.valueOf(cleanup.countEligibleToRemove()));
        MessageUtil.send(sender, MessageUtil.info("&7Protected: players, named, tamed, villagers, armor stands, frames, vehicles, bosses."));
        MessageUtil.send(sender, MessageUtil.info("&7Running cleanup on the main thread..."));

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (!isSenderAvailable(sender)) {
                return;
            }
            runCleanupConfirm(sender);
        });
    }

    private void runCleanupConfirm(CommandSender sender) {
        CleanupConfig cleanup = pluginConfig.getCleanup();

        OptionalLong cooldownRemaining = CleanupCooldownService.remainingSeconds(
                plugin.getCleanupHistory(),
                cleanup.getCooldownSeconds()
        );
        if (cooldownRemaining.isPresent()) {
            MessageUtil.send(sender, MessageUtil.error(String.format(
                    Locale.ROOT,
                    "&cCleanup is on cooldown. Try again in &f%s&c.",
                    CleanupCooldownService.formatRemaining(cooldownRemaining.getAsLong())
            )));
            MessageUtil.sendFooter(sender);
            return;
        }

        CleanupExecuteResult result = CleanupExecuteService.execute(cleanup, sender.getName());
        plugin.getCleanupHistory().record(result);

        if (cleanup.isLogActions()) {
            CleanupActionLogger.log(plugin, result);
        }

        MessageUtil.sendSection(sender, "Results");
        MessageUtil.sendStat(sender, "Dropped items removed", String.valueOf(result.droppedItemsRemoved()));
        MessageUtil.sendStat(sender, "Hostile mobs removed", String.valueOf(result.hostileMobsRemoved()));
        MessageUtil.sendStat(sender, "Passive mobs removed", String.valueOf(result.passiveMobsRemoved()));
        MessageUtil.sendStat(sender, "Total removed", String.valueOf(result.totalRemoved()));
        MessageUtil.blank(sender);

        MessageUtil.StatusLevel level = result.totalRemoved() > 0
                ? MessageUtil.StatusLevel.GOOD
                : MessageUtil.StatusLevel.WARNING;
        MessageUtil.send(sender, MessageUtil.overallStatus(
                level,
                result.totalRemoved() > 0
                        ? "Cleanup finished."
                        : "No entities matched cleanup rules."
        ));
        MessageUtil.sendFooter(sender);
    }

    private void sendLatestCleanupSummary(CommandSender sender, CleanupConfig cleanup) {
        if (!plugin.getCleanupHistory().hasLastResult()) {
            return;
        }

        CleanupExecuteResult last = plugin.getCleanupHistory().getLastResult();
        MessageUtil.blank(sender);
        MessageUtil.sendSection(sender, "Latest cleanup");
        MessageUtil.sendStat(sender, "When", last.formattedExecutedAt());
        MessageUtil.sendStat(sender, "Executor", last.executedBy());
        MessageUtil.sendStat(sender, "Worlds scanned", String.valueOf(last.worldsScanned()));
        MessageUtil.sendStat(sender, "Total removed", String.valueOf(last.totalRemoved()));

        OptionalLong cooldownRemaining = CleanupCooldownService.remainingSeconds(
                plugin.getCleanupHistory(),
                cleanup.getCooldownSeconds()
        );
        if (cooldownRemaining.isPresent()) {
            MessageUtil.sendStat(
                    sender,
                    "Confirm cooldown",
                    CleanupCooldownService.formatRemaining(cooldownRemaining.getAsLong()) + " remaining"
            );
        } else {
            MessageUtil.sendStat(sender, "Confirm cooldown", "ready");
        }
    }

    private static String yesNo(boolean value) {
        return value ? "yes" : "no";
    }

    private static boolean isSenderAvailable(CommandSender sender) {
        if (sender instanceof Player player) {
            return player.isOnline();
        }
        return true;
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

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (!isSenderAvailable(sender)) {
                return;
            }
            runChunkAnalysis(sender);
        });
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
            sendChunkRankHeader(sender, rank, result);
            MessageUtil.sendStat(sender, "Heaviness score", String.format("%.2f", result.heavinessScore));
            MessageUtil.sendStat(sender, "Total entities", String.valueOf(result.totalEntities));
            MessageUtil.sendStat(sender, "Dropped items", String.valueOf(result.droppedItems));
            MessageUtil.sendStat(sender, "Mobs", String.valueOf(result.mobs));
            MessageUtil.sendStat(sender, "Tile entities", String.valueOf(result.tileEntities));
            MessageUtil.sendStat(sender, "Hoppers", String.valueOf(result.hoppers));
            sendChunkRecommendations(sender, result);
            MessageUtil.blank(sender);
            rank++;
        }

        sendChunksOverviewRecommendations(sender, topResults);
        MessageUtil.sendFooter(sender);
    }

    private void sendChunkRankHeader(CommandSender sender, int rank, ChunkAnalysisResult result) {
        boolean clickable = pluginConfig.isChunkTeleportEnabled()
                && Permissions.canTpChunk(sender)
                && sender instanceof Player;
        MessageUtil.send(sender, MessageUtil.chunkRankHeader(rank, result, clickable));
    }

    private void handleTpChunk(CommandSender sender, String[] args) {
        if (!Permissions.canTpChunk(sender)) {
            MessageUtil.send(sender, MessageUtil.permissionDenied(Permissions.TPCHUNK));
            return;
        }

        if (!pluginConfig.isChunkTeleportEnabled()) {
            MessageUtil.send(sender, MessageUtil.error("&cChunk teleport is disabled in config.yml."));
            return;
        }

        if (!(sender instanceof Player player)) {
            MessageUtil.send(sender, MessageUtil.error("&cOnly players can teleport to chunks."));
            return;
        }

        if (args.length < 4) {
            MessageUtil.send(sender, MessageUtil.error("&cUsage: /doctor tpchunk <world> <chunkX> <chunkZ>"));
            return;
        }

        String worldName = ChunkTeleportService.normalizeWorldName(args[1]);
        int chunkX;
        int chunkZ;
        try {
            chunkX = Integer.parseInt(args[2]);
            chunkZ = Integer.parseInt(args[3]);
        } catch (NumberFormatException exception) {
            MessageUtil.send(sender, MessageUtil.error("&cChunk coordinates must be whole numbers."));
            return;
        }

        ChunkTeleportService.TeleportResult result = ChunkTeleportService.teleportToChunk(player, worldName, chunkX, chunkZ);
        if (result.success()) {
            MessageUtil.send(sender, MessageUtil.info(String.format(
                    Locale.ROOT,
                    "&aTeleported to &f%s &achunk [&f%d, %d&a] (&f%d, %d, %d&a).",
                    result.worldName(),
                    result.chunkX(),
                    result.chunkZ(),
                    result.blockX(),
                    result.y(),
                    result.blockZ()
            )));
        } else {
            MessageUtil.send(sender, MessageUtil.error("&c" + result.message()));
        }
    }

    private void sendChunkRecommendations(CommandSender sender, ChunkAnalysisResult result) {
        if (!pluginConfig.isRecommendationsEnabled()) {
            return;
        }
        List<String> tips = result.getRecommendations();
        if (tips.isEmpty()) {
            return;
        }
        MessageUtil.sendSection(sender, "Recommendations");
        for (String tip : tips) {
            MessageUtil.send(sender, MessageUtil.recommendationBullet(tip));
        }
    }

    private void sendChunksOverviewRecommendations(CommandSender sender, List<ChunkAnalysisResult> topResults) {
        List<String> overview = recommendationService.forChunksOverview(topResults, pluginConfig);
        if (overview.isEmpty()) {
            return;
        }
        MessageUtil.sendSection(sender, "Suggestions");
        for (String tip : overview) {
            MessageUtil.send(sender, MessageUtil.recommendationBullet(tip));
        }
    }

    private void handleExport(CommandSender sender) {
        if (!Permissions.canExport(sender)) {
            MessageUtil.send(sender, MessageUtil.permissionDenied(Permissions.EXPORT));
            return;
        }

        MessageUtil.send(sender, MessageUtil.info("&7Generating diagnostic report (read-only, no cleanup)..."));
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (!isSenderAvailable(sender)) {
                return;
            }
            try {
                String filename = ReportExporter.export(
                        plugin,
                        ServerStats.collect(pluginEnabledAtMillis),
                        pluginConfig,
                        recommendationService,
                        chunkAnalyzerService
                );
                MessageUtil.sendHeader(sender, "Export");
                MessageUtil.send(sender, MessageUtil.reportGenerated(filename));
                MessageUtil.sendFooter(sender);
            } catch (IOException exception) {
                plugin.getLogger().log(Level.SEVERE, "Failed to export ServerDoctor report", exception);
                MessageUtil.send(sender, MessageUtil.error("&cFailed to generate report. Check the server console for details."));
            }
        });
    }

    private void handleReload(CommandSender sender) {
        if (!Permissions.canReload(sender)) {
            MessageUtil.send(sender, MessageUtil.permissionDenied(Permissions.RELOAD));
            return;
        }
        pluginConfig.load();
        alertService.start();
        lagSpikeDetectorService.start();
        updateCheckerService.onReload();
        performanceHistoryService.start();
        scheduledReportService.start();
        MessageUtil.send(sender, MessageUtil.reloadSuccess());
    }

    private void sendSpikesStatus(CommandSender sender) {
        LagSpikeConfig lagSpike = pluginConfig.getLagSpike();
        LagSpikeHistory history = lagSpikeDetectorService.getHistory();

        MessageUtil.sendHeader(sender, "Lag Spike Detection");
        MessageUtil.sendSection(sender, "Settings");
        MessageUtil.sendStat(sender, "Detection enabled", yesNo(lagSpike.isEnabled()));
        MessageUtil.sendStat(sender, "Check interval", lagSpike.getCheckIntervalSeconds() + " seconds");
        MessageUtil.sendStat(sender, "TPS drop threshold", String.format(Locale.ROOT, "%.1f", lagSpike.getTpsDropThreshold()));
        MessageUtil.sendStat(sender, "MSPT spike threshold", String.format(Locale.ROOT, "%.1f ms", lagSpike.getMsptSpikeThreshold()));
        MessageUtil.sendStat(sender, "Alert cooldown", lagSpike.getAlertCooldownSeconds() + " seconds");
        MessageUtil.sendStat(sender, "Log spikes", yesNo(lagSpike.isLogSpikes()));
        MessageUtil.sendStat(sender, "Alert permission", Permissions.ALERTS);
        MessageUtil.blank(sender);

        OptionalLong cooldownRemaining = history.remainingCooldownSeconds(lagSpike.getAlertCooldownSeconds());
        MessageUtil.sendSection(sender, "Alert cooldown status");
        if (cooldownRemaining.isPresent()) {
            MessageUtil.sendStat(
                    sender,
                    "Next alert allowed in",
                    CleanupCooldownService.formatRemaining(cooldownRemaining.getAsLong())
            );
        } else {
            MessageUtil.sendStat(sender, "Next alert", "ready");
        }
        MessageUtil.blank(sender);

        if (history.hasLatestSpike()) {
            LagSpikeEvent latest = history.getLatestSpike();
            MessageUtil.sendSection(sender, "Latest spike");
            MessageUtil.sendStat(sender, "Detected at", latest.formattedDetectedAt());
            MessageUtil.sendStat(sender, "Trigger", latest.triggerSummary());
            MessageUtil.sendStat(sender, "TPS", ServerStats.formatTps(latest.tps()));
            MessageUtil.sendStat(sender, "MSPT", ServerStats.formatMspt(latest.mspt()));
            MessageUtil.sendStat(sender, "Memory", String.format(Locale.ROOT, "%.1f%%", latest.memoryUsagePercent()));
            MessageUtil.sendStat(sender, "Entities", String.format(Locale.ROOT, "%,d", latest.entityCount()));
            MessageUtil.sendStat(sender, "Loaded chunks", String.valueOf(latest.loadedChunkCount()));
        } else {
            MessageUtil.sendSection(sender, "Latest spike");
            MessageUtil.send(sender, MessageUtil.info("&7No lag spikes detected since server start."));
        }

        MessageUtil.sendFooter(sender);
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
            MessageUtil.send(sender, MessageUtil.info("&7Discord will receive health and lag spike alerts (webhook URL is never shown in chat)."));
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
        MessageUtil.sendStat(sender, "ServerDoctor uptime", stats.uptime);
        MessageUtil.sendStat(sender, "ServerDoctor version", PluginAbout.version(plugin));
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
        sendLatestLagSpikeMention(sender);
        sendHealthRecommendations(sender, stats);
        MessageUtil.sendFooter(sender);
    }

    private void sendLatestLagSpikeMention(CommandSender sender) {
        LagSpikeHistory history = lagSpikeDetectorService.getHistory();
        if (!history.hasLatestSpike()) {
            return;
        }

        LagSpikeEvent latest = history.getLatestSpike();
        MessageUtil.blank(sender);
        MessageUtil.sendSection(sender, "Latest lag spike");
        MessageUtil.send(sender, MessageUtil.warningBullet(String.format(
                Locale.ROOT,
                "Detected at %s (%s) — TPS %s, MSPT %s, memory %.1f%%, %,d entities, %,d chunks. Use &f/doctor spikes &efor details.",
                latest.formattedDetectedAt(),
                latest.triggerSummary(),
                ServerStats.formatTps(latest.tps()),
                ServerStats.formatMspt(latest.mspt()),
                latest.memoryUsagePercent(),
                latest.entityCount(),
                latest.loadedChunkCount()
        )));
    }

    private void sendHealthRecommendations(CommandSender sender, ServerStats stats) {
        List<String> tips = recommendationService.forHealthReport(stats, pluginConfig);
        if (tips.isEmpty()) {
            return;
        }
        MessageUtil.blank(sender);
        MessageUtil.sendSection(sender, "Recommendations");
        MessageUtil.send(sender, MessageUtil.info("&7Advisory only — review in-game before changing builds."));
        for (String tip : tips) {
            MessageUtil.send(sender, MessageUtil.recommendationBullet(tip));
        }
    }

    private List<String> buildWarnings(ServerStats stats) {
        List<String> warnings = new ArrayList<>();
        for (AlertType type : HealthChecker.findActiveAlerts(stats, pluginConfig)) {
            warnings.add(HealthChecker.formatReportWarning(type, stats, pluginConfig));
        }
        return warnings;
    }
}
