package com.jimm.serverdoctor;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.EnumMap;
import java.util.Map;

public final class AlertService {

    private static final long ALERT_COOLDOWN_MS = 5 * 60 * 1000L;

    private final ServerDoctorPlugin plugin;
    private final PluginConfig pluginConfig;
    private final DiscordWebhookService discordWebhookService;
    private final Map<AlertType, Long> lastAlertSentAt = new EnumMap<>(AlertType.class);
    private BukkitTask checkTask;

    public AlertService(
            ServerDoctorPlugin plugin,
            PluginConfig pluginConfig,
            DiscordWebhookService discordWebhookService
    ) {
        this.plugin = plugin;
        this.pluginConfig = pluginConfig;
        this.discordWebhookService = discordWebhookService;
    }

    public void start() {
        stop();

        if (!pluginConfig.isAlertsEnabled()) {
            return;
        }

        long intervalSeconds = pluginConfig.getAlertCheckIntervalSeconds();
        if (intervalSeconds < 1) {
            intervalSeconds = 1;
        }

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

    private void runCheck() {
        if (!pluginConfig.isAlertsEnabled()) {
            return;
        }

        ServerStats stats = ServerStats.collect(plugin.getEnabledAtMillis());

        for (AlertType type : HealthChecker.findActiveAlerts(stats, pluginConfig)) {
            if (shouldSendAlert(type)) {
                sendAlertToPlayers(type, stats);
                discordWebhookService.sendAlert(type, stats);
                lastAlertSentAt.put(type, System.currentTimeMillis());
            }
        }

        for (AlertType type : AlertType.values()) {
            if (!HealthChecker.findActiveAlerts(stats, pluginConfig).contains(type)) {
                lastAlertSentAt.remove(type);
            }
        }
    }

    private boolean shouldSendAlert(AlertType type) {
        Long lastSent = lastAlertSentAt.get(type);
        if (lastSent == null) {
            return true;
        }
        return System.currentTimeMillis() - lastSent >= ALERT_COOLDOWN_MS;
    }

    private void sendAlertToPlayers(AlertType type, ServerStats stats) {
        String detail = HealthChecker.formatBroadcastAlert(type, stats, pluginConfig);
        Component message = MessageUtil.alertBroadcast(detail);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (Permissions.canAlerts(player)) {
                AdventureMessages.send(player, message);
            }
        }
    }

    public static long getAlertCooldownMinutes() {
        return ALERT_COOLDOWN_MS / 60_000L;
    }
}
