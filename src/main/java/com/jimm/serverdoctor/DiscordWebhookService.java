package com.jimm.serverdoctor;

import org.bukkit.Bukkit;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.logging.Level;

/**
 * Sends health warnings to a Discord channel using a webhook URL.
 * <p>
 * Uses Java's built-in {@link HttpClient} and runs requests asynchronously
 * so the Minecraft main thread is never blocked.
 */
public final class DiscordWebhookService {

    private final ServerDoctorPlugin plugin;
    private final PluginConfig pluginConfig;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public DiscordWebhookService(ServerDoctorPlugin plugin, PluginConfig pluginConfig) {
        this.plugin = plugin;
        this.pluginConfig = pluginConfig;
    }

    /**
     * Queues a Discord message for the same alert that was just sent in-game.
     * Call only when the normal alert cooldown allows a new alert.
     */
    public void sendAlert(AlertType type, ServerStats stats) {
        if (!pluginConfig.shouldSendDiscordAlerts()) {
            return;
        }

        String webhookUrl = pluginConfig.getDiscordWebhookUrl().trim();
        String jsonBody = buildJsonPayload(type, stats);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(webhookUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .whenComplete((response, error) -> {
                    if (error != null) {
                        plugin.getLogger().log(Level.WARNING, "Discord webhook request failed", error);
                        return;
                    }
                    int statusCode = response.statusCode();
                    if (statusCode < 200 || statusCode >= 300) {
                        plugin.getLogger().warning(
                                "Discord webhook returned HTTP " + statusCode + " (check your webhook URL)"
                        );
                    }
                });
    }

    /**
     * Queues a Discord message for a lag spike alert.
     */
    public void sendLagSpike(LagSpikeEvent spike, LagSpikeConfig lagSpikeConfig) {
        if (!pluginConfig.shouldSendDiscordAlerts()) {
            return;
        }

        String webhookUrl = pluginConfig.getDiscordWebhookUrl().trim();
        String jsonBody = buildLagSpikeJsonPayload(spike, lagSpikeConfig);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(webhookUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .whenComplete((response, error) -> {
                    if (error != null) {
                        plugin.getLogger().log(Level.WARNING, "Discord lag spike webhook failed", error);
                        return;
                    }
                    int statusCode = response.statusCode();
                    if (statusCode < 200 || statusCode >= 300) {
                        plugin.getLogger().warning(
                                "Discord lag spike webhook returned HTTP " + statusCode
                        );
                    }
                });
    }

    private String buildLagSpikeJsonPayload(LagSpikeEvent spike, LagSpikeConfig lagSpikeConfig) {
        String serverName = Bukkit.getServer().getName();
        String time = spike.formattedDetectedAt();
        String title = pluginConfig.getDiscordAlertTitle() + " — Lag Spike";

        return "{"
                + "\"username\":\"ServerDoctor\","
                + "\"embeds\":[{"
                + "\"title\":\"" + escapeJson(title) + "\","
                + "\"color\":15105570,"
                + "\"fields\":["
                + field("Server", serverName, true)
                + "," + field("Trigger", spike.triggerSummary(), true)
                + "," + field("TPS", String.format("%.2f (limit %.1f)", spike.tps(), lagSpikeConfig.getTpsDropThreshold()), true)
                + "," + field("MSPT", String.format("%.2f ms (limit %.1f ms)", spike.mspt(), lagSpikeConfig.getMsptSpikeThreshold()), true)
                + "," + field("Memory", String.format("%.1f%%", spike.memoryUsagePercent()), true)
                + "," + field("Entities", String.valueOf(spike.entityCount()), true)
                + "," + field("Loaded chunks", String.valueOf(spike.loadedChunkCount()), true)
                + "," + field("Time", time, false)
                + "]"
                + "}]"
                + "}";
    }

    private String buildJsonPayload(AlertType type, ServerStats stats) {
        String serverName = Bukkit.getServer().getName();
        String alertType = HealthChecker.alertTypeName(type);
        String currentValue = HealthChecker.formatCurrentValue(type, stats);
        String threshold = HealthChecker.formatThresholdValue(type, pluginConfig);
        String time = Instant.now().toString();
        String title = pluginConfig.getDiscordAlertTitle();

        return "{"
                + "\"username\":\"ServerDoctor\","
                + "\"embeds\":[{"
                + "\"title\":\"" + escapeJson(title) + "\","
                + "\"color\":15158332,"
                + "\"fields\":["
                + field("Server", serverName, true)
                + "," + field("Alert Type", alertType, true)
                + "," + field("Current Value", currentValue, true)
                + "," + field("Threshold", threshold, true)
                + "," + field("Time", time, false)
                + "]"
                + "}]"
                + "}";
    }

    private static String field(String name, String value, boolean inline) {
        return "{\"name\":\"" + escapeJson(name) + "\","
                + "\"value\":\"" + escapeJson(value) + "\","
                + "\"inline\":" + inline + "}";
    }

    private static String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}
