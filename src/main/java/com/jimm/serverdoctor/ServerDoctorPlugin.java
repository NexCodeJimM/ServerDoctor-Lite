package com.jimm.serverdoctor;

import org.bukkit.plugin.java.JavaPlugin;

public final class ServerDoctorPlugin extends JavaPlugin {

    private long enabledAtMillis;
    private PluginConfig pluginConfig;
    private DiscordWebhookService discordWebhookService;
    private ChunkAnalyzerService chunkAnalyzerService;
    private RecommendationService recommendationService;
    private final CleanupHistory cleanupHistory = new CleanupHistory();
    private final LagSpikeHistory lagSpikeHistory = new LagSpikeHistory();
    private AlertService alertService;
    private LagSpikeDetectorService lagSpikeDetectorService;

    @Override
    public void onEnable() {
        enabledAtMillis = System.currentTimeMillis();

        PluginDataFolders.ensureReady(this);

        pluginConfig = new PluginConfig(this);
        pluginConfig.load();

        discordWebhookService = new DiscordWebhookService(this, pluginConfig);
        recommendationService = new RecommendationService();
        chunkAnalyzerService = new ChunkAnalyzerService(pluginConfig, recommendationService);
        alertService = new AlertService(this, pluginConfig, discordWebhookService);
        alertService.start();

        lagSpikeDetectorService = new LagSpikeDetectorService(
                this,
                pluginConfig,
                discordWebhookService,
                lagSpikeHistory
        );
        lagSpikeDetectorService.start();

        registerCommand(
                "doctor",
                new DoctorCommand(
                        this,
                        pluginConfig,
                        alertService,
                        chunkAnalyzerService,
                        recommendationService,
                        lagSpikeDetectorService
                )
        );

        getLogger().info("ServerDoctor Lite enabled.");
    }

    @Override
    public void onDisable() {
        if (alertService != null) {
            alertService.stop();
        }
        if (lagSpikeDetectorService != null) {
            lagSpikeDetectorService.stop();
        }
        getLogger().info("ServerDoctor disabled.");
    }

    public long getEnabledAtMillis() {
        return enabledAtMillis;
    }

    public CleanupHistory getCleanupHistory() {
        return cleanupHistory;
    }

    public LagSpikeHistory getLagSpikeHistory() {
        return lagSpikeHistory;
    }
}
