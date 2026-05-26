package com.jimm.serverdoctor;

import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

/**
 * Periodically generates diagnostic reports and optionally sends a short Discord summary.
 */
public final class ScheduledReportService {

    private static final DateTimeFormatter DISPLAY_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ServerDoctorPlugin plugin;
    private final PluginConfig pluginConfig;
    private final DiscordWebhookService discordWebhookService;
    private final ChunkAnalyzerService chunkAnalyzerService;
    private final RecommendationService recommendationService;

    private BukkitTask reportTask;
    private volatile long nextRunAtMillis;
    private volatile long lastRunAtMillis;
    private volatile String lastReportFilename;

    public ScheduledReportService(
            ServerDoctorPlugin plugin,
            PluginConfig pluginConfig,
            DiscordWebhookService discordWebhookService,
            ChunkAnalyzerService chunkAnalyzerService,
            RecommendationService recommendationService
    ) {
        this.plugin = plugin;
        this.pluginConfig = pluginConfig;
        this.discordWebhookService = discordWebhookService;
        this.chunkAnalyzerService = chunkAnalyzerService;
        this.recommendationService = recommendationService;
    }

    public void start() {
        stop();

        ScheduledReportsConfig config = pluginConfig.getScheduledReports();
        if (!config.isEnabled()) {
            nextRunAtMillis = 0;
            return;
        }

        long intervalTicks = Math.max(1L, config.getIntervalHours()) * 60L * 60L * 20L;
        nextRunAtMillis = System.currentTimeMillis() + config.intervalMillis();

        reportTask = plugin.getServer().getScheduler().runTaskTimer(
                plugin,
                this::runScheduledReport,
                intervalTicks,
                intervalTicks
        );
    }

    public void stop() {
        if (reportTask != null) {
            reportTask.cancel();
            reportTask = null;
        }
    }

    private void runScheduledReport() {
        ScheduledReportsConfig config = pluginConfig.getScheduledReports();
        if (!config.isEnabled()) {
            return;
        }

        nextRunAtMillis = System.currentTimeMillis() + config.intervalMillis();

        try {
            ServerStats stats = ServerStats.collect(plugin.getEnabledAtMillis());
            String filename = null;

            if (config.isSaveToFile()) {
                filename = ReportExporter.exportScheduled(
                        plugin,
                        stats,
                        pluginConfig,
                        recommendationService,
                        chunkAnalyzerService
                );
                lastReportFilename = filename;
            }

            lastRunAtMillis = System.currentTimeMillis();

            if (config.isSendToDiscord() && pluginConfig.shouldSendDiscordAlerts()) {
                List<AlertType> activeAlerts = HealthChecker.findActiveAlerts(stats, pluginConfig);
                discordWebhookService.sendScheduledReportSummary(stats, activeAlerts.size(), filename);
            } else if (config.isSendToDiscord()) {
                plugin.getLogger().info(
                        "Scheduled report Discord summary skipped — enable discord-alerts and set a webhook URL."
                );
            }

            plugin.getLogger().info(buildConsoleLogMessage(filename));
        } catch (Exception exception) {
            plugin.getLogger().log(Level.WARNING, "Scheduled diagnostic report failed", exception);
        }
    }

    private String buildConsoleLogMessage(String filename) {
        if (filename != null) {
            return "ServerDoctor scheduled diagnostic report saved: scheduled-reports/" + filename;
        }
        return "ServerDoctor scheduled diagnostic report completed (file save disabled).";
    }

    public void sendStatus(CommandSender sender) {
        ScheduledReportsConfig config = pluginConfig.getScheduledReports();

        MessageUtil.sendHeader(sender, "Scheduled Reports");
        MessageUtil.sendStat(sender, "Enabled", yesNo(config.isEnabled()));
        MessageUtil.sendStat(sender, "Interval", config.getIntervalHours() + " hours");
        MessageUtil.sendStat(sender, "Save to file", yesNo(config.isSaveToFile()));
        MessageUtil.sendStat(sender, "Discord summary", yesNo(config.isSendToDiscord()));

        if (config.isSendToDiscord()) {
            MessageUtil.sendStat(
                    sender,
                    "Discord ready",
                    yesNo(pluginConfig.shouldSendDiscordAlerts())
            );
            if (!pluginConfig.shouldSendDiscordAlerts()) {
                MessageUtil.send(sender, MessageUtil.info(
                        "&7Enable &fdiscord-alerts &7and set &fdiscord-webhook-url &7for Discord summaries."
                ));
            }
        }

        MessageUtil.blank(sender);
        MessageUtil.sendSection(sender, "Schedule");

        if (!config.isEnabled()) {
            MessageUtil.sendStat(sender, "Next report", "not scheduled (disabled)");
        } else if (nextRunAtMillis > 0) {
            MessageUtil.sendStat(sender, "Next report", formatTime(nextRunAtMillis));
        } else {
            MessageUtil.sendStat(sender, "Next report", "pending first interval after reload");
        }

        if (lastRunAtMillis > 0) {
            MessageUtil.sendStat(sender, "Last generated", formatTime(lastRunAtMillis));
            if (lastReportFilename != null) {
                MessageUtil.sendStat(sender, "Last file", "scheduled-reports/" + lastReportFilename);
            }
        } else {
            MessageUtil.sendStat(sender, "Last generated", "none this session");
        }

        MessageUtil.sendFooter(sender);
    }

    private static String formatTime(long epochMillis) {
        LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault());
        return time.format(DISPLAY_TIME);
    }

    private static String yesNo(boolean value) {
        return value ? "yes" : "no";
    }
}
