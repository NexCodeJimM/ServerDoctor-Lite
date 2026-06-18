package com.jimm.serverdoctor;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Locale;

/**
 * Periodically monitors performance and alerts admins when a lag spike is detected.
 */
public final class LagSpikeDetectorService {

    private final ServerDoctorPlugin plugin;
    private final PluginConfig pluginConfig;
    private final DiscordWebhookService discordWebhookService;
    private final LagSpikeHistory lagSpikeHistory;
    private BukkitTask checkTask;

    public LagSpikeDetectorService(
            ServerDoctorPlugin plugin,
            PluginConfig pluginConfig,
            DiscordWebhookService discordWebhookService,
            LagSpikeHistory lagSpikeHistory
    ) {
        this.plugin = plugin;
        this.pluginConfig = pluginConfig;
        this.discordWebhookService = discordWebhookService;
        this.lagSpikeHistory = lagSpikeHistory;
    }

    public void start() {
        stop();

        LagSpikeConfig config = pluginConfig.getLagSpike();
        if (!config.isEnabled()) {
            return;
        }

        long intervalSeconds = Math.max(1, config.getCheckIntervalSeconds());
        long intervalTicks = intervalSeconds * 20L;
        checkTask = plugin.getServer().getScheduler().runTaskTimer(
                plugin,
                this::runCheck,
                intervalTicks,
                intervalTicks
        );
    }

    public void stop() {
        if (checkTask != null) {
            checkTask.cancel();
            checkTask = null;
        }
    }

    public LagSpikeHistory getHistory() {
        return lagSpikeHistory;
    }

    private void runCheck() {
        LagSpikeConfig config = pluginConfig.getLagSpike();
        if (!config.isEnabled()) {
            return;
        }

        ServerStats stats = ServerStats.collect(plugin.getEnabledAtMillis());
        LagSpikeEvent spike = LagSpikeDetector.detect(stats, config);
        if (spike == null) {
            return;
        }

        lagSpikeHistory.recordLatest(spike);

        InvestigationSessionService investigation = plugin.getInvestigationSessionService();
        if (investigation != null) {
            investigation.onLagSpike(spike);
        }

        if (!lagSpikeHistory.shouldSendAlert(config.getAlertCooldownSeconds())) {
            return;
        }

        sendAlertToPlayers(spike, config);
        discordWebhookService.sendLagSpike(spike, config);

        if (config.isLogSpikes()) {
            LagSpikeLogger.log(plugin, spike);
        }

        lagSpikeHistory.markAlertSent();
    }

    private void sendAlertToPlayers(LagSpikeEvent spike, LagSpikeConfig config) {
        String detail = formatBroadcastMessage(spike, config);
        Component message = MessageUtil.alertBroadcast(detail);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (Permissions.canAlerts(player)) {
                AdventureMessages.send(player, message);
            }
        }
    }

    public static String formatBroadcastMessage(LagSpikeEvent spike, LagSpikeConfig config) {
        return String.format(
                Locale.ROOT,
                "Lag spike (%s): TPS %.2f (limit %.1f), MSPT %.2f ms (limit %.1f ms)",
                spike.triggerSummary(),
                spike.tps(),
                config.getTpsDropThreshold(),
                spike.mspt(),
                config.getMsptSpikeThreshold()
        );
    }
}
