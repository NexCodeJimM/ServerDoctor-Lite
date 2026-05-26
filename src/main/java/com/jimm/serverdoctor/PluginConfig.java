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
    private boolean recommendationsEnabled;
    private boolean chunkTeleportEnabled;
    private final CleanupConfig cleanup = new CleanupConfig();
    private final LagSpikeConfig lagSpike = new LagSpikeConfig();
    private final UpdateCheckerConfig updateChecker = new UpdateCheckerConfig();
    private final PluginImpactScannerConfig pluginImpactScanner = new PluginImpactScannerConfig();
    private final HistoryConfig history = new HistoryConfig();
    private final ScheduledReportsConfig scheduledReports = new ScheduledReportsConfig();
    private final MessageConfig messages = new MessageConfig();

    public PluginConfig(ServerDoctorPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        FileConfiguration config = plugin.getConfig();
        memoryWarningPercent = ConfigDefaults.percent(config.getDouble("memory-warning-percent", 80.0), 80.0);
        entityWarningLimit = ConfigDefaults.atLeast(config.getLong("entity-warning-limit", 3000L), 1, 3000L);
        loadedChunkWarningLimit = ConfigDefaults.atLeast(config.getInt("loaded-chunk-warning-limit", 5000), 1, 5000);
        tpsWarningThreshold = ConfigDefaults.clamp(config.getDouble("tps-warning-threshold", 18.0), 0.1, 20.0, 18.0);
        msptWarningThreshold = ConfigDefaults.clamp(config.getDouble("mspt-warning-threshold", 50.0), 0.0, 1000.0, 50.0);
        alertsEnabled = config.getBoolean("alerts-enabled", true);
        alertCheckIntervalSeconds = ConfigDefaults.atLeast(config.getInt("alert-check-interval-seconds", 60), 1, 60);
        discordAlertsEnabled = config.getBoolean("discord-alerts-enabled", false);
        discordWebhookUrl = ConfigDefaults.nonNullString(config.getString("discord-webhook-url", ""), "").trim();
        discordAlertTitle = ConfigDefaults.nonNullString(
                config.getString("discord-alert-title", "ServerDoctor Alert"),
                "ServerDoctor Alert"
        );
        chunkAnalyzerEnabled = config.getBoolean("chunk-analyzer-enabled", true);
        chunkAnalyzerTopLimit = ConfigDefaults.atLeast(config.getInt("chunk-analyzer-top-limit", 5), 1, 5);
        chunkWarningEntityLimit = ConfigDefaults.atLeast(config.getInt("chunk-warning-entity-limit", 100), 1, 100);
        chunkWarningDroppedItemLimit = ConfigDefaults.atLeast(config.getInt("chunk-warning-dropped-item-limit", 50), 1, 50);
        chunkWarningHopperLimit = ConfigDefaults.atLeast(config.getInt("chunk-warning-hopper-limit", 30), 1, 30);
        recommendationsEnabled = config.getBoolean("recommendations-enabled", true);
        chunkTeleportEnabled = config.getBoolean("chunk-teleport-enabled", true);

        cleanup.load(config);
        lagSpike.load(config);
        updateChecker.load(config);
        pluginImpactScanner.load(config);
        history.load(config);
        scheduledReports.load(config);
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

    public boolean isRecommendationsEnabled() {
        return recommendationsEnabled;
    }

    public boolean isChunkTeleportEnabled() {
        return chunkTeleportEnabled;
    }

    public CleanupConfig getCleanup() {
        return cleanup;
    }

    public LagSpikeConfig getLagSpike() {
        return lagSpike;
    }

    public UpdateCheckerConfig getUpdateChecker() {
        return updateChecker;
    }

    public PluginImpactScannerConfig getPluginImpactScanner() {
        return pluginImpactScanner;
    }

    public HistoryConfig getHistory() {
        return history;
    }

    public ScheduledReportsConfig getScheduledReports() {
        return scheduledReports;
    }
}
