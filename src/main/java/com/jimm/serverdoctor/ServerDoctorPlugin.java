package com.jimm.serverdoctor;

import org.bukkit.plugin.java.JavaPlugin;

public final class ServerDoctorPlugin extends JavaPlugin {

    private long enabledAtMillis;
    private PluginConfig pluginConfig;
    private DiscordWebhookService discordWebhookService;
    private ChunkAnalyzerService chunkAnalyzerService;
    private AlertService alertService;

    @Override
    public void onEnable() {
        enabledAtMillis = System.currentTimeMillis();

        pluginConfig = new PluginConfig(this);
        pluginConfig.load();

        discordWebhookService = new DiscordWebhookService(this, pluginConfig);
        chunkAnalyzerService = new ChunkAnalyzerService(pluginConfig);
        alertService = new AlertService(this, pluginConfig, discordWebhookService);
        alertService.start();

        registerCommand("doctor", new DoctorCommand(this, pluginConfig, alertService, chunkAnalyzerService));

        getLogger().info("ServerDoctor Lite enabled. Commands: /doctor, report, alerts, export, reload, discord, chunks.");
    }

    @Override
    public void onDisable() {
        if (alertService != null) {
            alertService.stop();
        }
        getLogger().info("ServerDoctor disabled.");
    }

    public long getEnabledAtMillis() {
        return enabledAtMillis;
    }
}
