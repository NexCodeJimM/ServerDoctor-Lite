package com.jimm.serverdoctor;

import org.bukkit.configuration.file.FileConfiguration;

public final class PluginConfig {

    private final ServerDoctorPlugin plugin;

    private double memoryWarningPercent;
    private long entityWarningLimit;
    private int loadedChunkWarningLimit;
    private double tpsWarningThreshold;
    private double msptWarningThreshold;
    private boolean alertsEnabled;
    private int alertCheckIntervalSeconds;
    private boolean discordAlertsEnabled;
    private String discordWebhookUrl;
    private String discordAlertTitle;
    private boolean chunkAnalyzerEnabled;
    private int chunkAnalyzerTopLimit;
    private int chunkWarningEntityLimit;
    private int chunkWarningDroppedItemLimit;
    private int chunkWarningHopperLimit;
    private final MessageConfig messages = new MessageConfig();

    public PluginConfig(ServerDoctorPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        FileConfiguration config = plugin.getConfig();
        memoryWarningPercent = config.getDouble("memory-warning-percent", 80.0);
        entityWarningLimit = config.getLong("entity-warning-limit", 3000L);
        loadedChunkWarningLimit = config.getInt("loaded-chunk-warning-limit", 5000);
        tpsWarningThreshold = config.getDouble("tps-warning-threshold", 18.0);
        msptWarningThreshold = config.getDouble("mspt-warning-threshold", 50.0);
        alertsEnabled = config.getBoolean("alerts-enabled", true);
        alertCheckIntervalSeconds = config.getInt("alert-check-interval-seconds", 60);
        discordAlertsEnabled = config.getBoolean("discord-alerts-enabled", false);
        discordWebhookUrl = config.getString("discord-webhook-url", "");
        discordAlertTitle = config.getString("discord-alert-title", "ServerDoctor Alert");
        chunkAnalyzerEnabled = config.getBoolean("chunk-analyzer-enabled", true);
        chunkAnalyzerTopLimit = config.getInt("chunk-analyzer-top-limit", 5);
        chunkWarningEntityLimit = config.getInt("chunk-warning-entity-limit", 100);
        chunkWarningDroppedItemLimit = config.getInt("chunk-warning-dropped-item-limit", 50);
        chunkWarningHopperLimit = config.getInt("chunk-warning-hopper-limit", 30);

        messages.load(config);
        MessageUtil.setMessages(messages);
    }

    public MessageConfig getMessages() {
        return messages;
    }

    public double getMemoryWarningPercent() {
        return memoryWarningPercent;
    }

    public long getEntityWarningLimit() {
        return entityWarningLimit;
    }

    public int getLoadedChunkWarningLimit() {
        return loadedChunkWarningLimit;
    }

    public double getTpsWarningThreshold() {
        return tpsWarningThreshold;
    }

    public double getMsptWarningThreshold() {
        return msptWarningThreshold;
    }

    public boolean isAlertsEnabled() {
        return alertsEnabled;
    }

    public int getAlertCheckIntervalSeconds() {
        return alertCheckIntervalSeconds;
    }

    public boolean isDiscordAlertsEnabled() {
        return discordAlertsEnabled;
    }

    public boolean isDiscordWebhookConfigured() {
        return discordWebhookUrl != null && !discordWebhookUrl.isBlank();
    }

    /** True when Discord alerts should be sent (enabled + URL set). */
    public boolean shouldSendDiscordAlerts() {
        return discordAlertsEnabled && isDiscordWebhookConfigured();
    }

    public String getDiscordAlertTitle() {
        return discordAlertTitle;
    }

    /** Used only by {@link DiscordWebhookService} — never show in chat. */
    String getDiscordWebhookUrl() {
        return discordWebhookUrl;
    }

    public boolean isChunkAnalyzerEnabled() {
        return chunkAnalyzerEnabled;
    }

    public int getChunkAnalyzerTopLimit() {
        return chunkAnalyzerTopLimit;
    }

    public int getChunkWarningEntityLimit() {
        return chunkWarningEntityLimit;
    }

    public int getChunkWarningDroppedItemLimit() {
        return chunkWarningDroppedItemLimit;
    }

    public int getChunkWarningHopperLimit() {
        return chunkWarningHopperLimit;
    }
}
