package com.jimm.serverdoctor;

import org.bukkit.command.PluginCommand;
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
    private UpdateCheckerService updateCheckerService;
    private PluginImpactScannerService pluginImpactScannerService;
    private PerformanceHistoryService performanceHistoryService;
    private ScheduledReportService scheduledReportService;

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

        updateCheckerService = new UpdateCheckerService(this, pluginConfig);
        getServer().getPluginManager().registerEvents(updateCheckerService, this);
        updateCheckerService.onEnable();

        pluginImpactScannerService = new PluginImpactScannerService(this, recommendationService);

        performanceHistoryService = new PerformanceHistoryService(
                this,
                pluginConfig,
                lagSpikeHistory,
                cleanupHistory
        );
        performanceHistoryService.start();

        scheduledReportService = new ScheduledReportService(
                this,
                pluginConfig,
                discordWebhookService,
                chunkAnalyzerService,
                recommendationService
        );
        scheduledReportService.start();

        DoctorCommand doctorCommand = new DoctorCommand(
                this,
                pluginConfig,
                alertService,
                chunkAnalyzerService,
                recommendationService,
                lagSpikeDetectorService,
                updateCheckerService,
                pluginImpactScannerService,
                performanceHistoryService,
                scheduledReportService
        );
        PluginCommand doctorPluginCommand = getCommand("doctor");
        if (doctorPluginCommand != null) {
            doctorPluginCommand.setExecutor(doctorCommand);
            doctorPluginCommand.setTabCompleter(doctorCommand);
        } else {
            getLogger().severe("Command 'doctor' is missing from plugin.yml — commands will not work.");
        }

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
        if (performanceHistoryService != null) {
            performanceHistoryService.stop();
        }
        if (scheduledReportService != null) {
            scheduledReportService.stop();
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

    public UpdateCheckerService getUpdateCheckerService() {
        return updateCheckerService;
    }
}
