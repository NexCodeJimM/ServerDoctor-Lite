package com.jimm.serverdoctor;

import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Locale;

/**
 * Periodically samples performance and serves {@code /doctor history} commands.
 */
public final class PerformanceHistoryService {

    private final ServerDoctorPlugin plugin;
    private final PluginConfig pluginConfig;
    private final PerformanceHistoryStore store = new PerformanceHistoryStore();
    private final LagSpikeHistory lagSpikeHistory;
    private final CleanupHistory cleanupHistory;
    private BukkitTask sampleTask;

    public PerformanceHistoryService(
            ServerDoctorPlugin plugin,
            PluginConfig pluginConfig,
            LagSpikeHistory lagSpikeHistory,
            CleanupHistory cleanupHistory
    ) {
        this.plugin = plugin;
        this.pluginConfig = pluginConfig;
        this.lagSpikeHistory = lagSpikeHistory;
        this.cleanupHistory = cleanupHistory;
    }

    public void start() {
        stop();
        HistoryConfig config = pluginConfig.getHistory();
        if (!config.isEnabled()) {
            return;
        }

        store.setMaxEntries(config.getMaxHistoryEntries());
        long intervalTicks = Math.max(1, config.getSampleIntervalSeconds()) * 20L;
        sampleTask = plugin.getServer().getScheduler().runTaskTimer(
                plugin,
                this::recordSample,
                intervalTicks,
                intervalTicks
        );
    }

    public void stop() {
        if (sampleTask != null) {
            sampleTask.cancel();
            sampleTask = null;
        }
    }

    public PerformanceHistoryStore getStore() {
        return store;
    }

    private void recordSample() {
        HistoryConfig config = pluginConfig.getHistory();
        if (!config.isEnabled()) {
            return;
        }

        store.setMaxEntries(config.getMaxHistoryEntries());
        PerformanceSnapshot snapshot = PerformanceSnapshotCollector.collect();
        store.add(snapshot);

        if (config.isSaveHistoryFiles()) {
            PerformanceHistoryFileLogger.append(plugin, snapshot);
        }
    }

    public void sendOverview(CommandSender sender) {
        HistoryConfig config = pluginConfig.getHistory();
        MessageUtil.sendHeader(sender, "Performance History");

        if (!config.isEnabled()) {
            MessageUtil.send(sender, MessageUtil.error("&cHistorical tracking is disabled in config.yml."));
            MessageUtil.sendFooter(sender);
            return;
        }

        MessageUtil.sendStat(sender, "Samples stored", String.valueOf(store.size()));
        MessageUtil.sendStat(
                sender,
                "Sample interval",
                config.getSampleIntervalSeconds() + " seconds"
        );
        MessageUtil.blank(sender);

        if (store.isEmpty()) {
            MessageUtil.send(sender, MessageUtil.info("&7No samples yet — wait for the next scheduled snapshot."));
            MessageUtil.sendFooter(sender);
            return;
        }

        PerformanceHistoryStore.PerformanceAverages averages = store.averages();
        MessageUtil.sendSection(sender, "Averages (tracked period)");
        MessageUtil.sendStat(sender, "TPS", averages.formatTps());
        MessageUtil.sendStat(sender, "MSPT", averages.formatMspt());
        MessageUtil.sendStat(sender, "Memory", averages.formatMemory());
        MessageUtil.sendStat(
                sender,
                "Loaded chunks",
                String.format(Locale.ROOT, "%,.0f", averages.averageLoadedChunks())
        );
        MessageUtil.sendStat(
                sender,
                "Entities",
                String.format(Locale.ROOT, "%,.0f", averages.averageEntities())
        );
        MessageUtil.blank(sender);

        MessageUtil.sendSection(sender, "Recent events (this session)");
        MessageUtil.sendStat(sender, "Lag spikes recorded", String.valueOf(lagSpikeHistory.getRecentSpikeCount()));
        MessageUtil.sendStat(sender, "Cleanup runs", String.valueOf(cleanupHistory.getCleanupCount()));
        MessageUtil.blank(sender);

        PerformanceTrendAnalyzer.PerformanceTrendSummary trend =
                PerformanceTrendAnalyzer.analyze(store.snapshots());
        MessageUtil.sendSection(sender, "Trend summary");
        MessageUtil.sendStat(sender, "TPS", trend.tpsTrend());
        MessageUtil.sendStat(sender, "MSPT", trend.msptTrend());
        MessageUtil.sendStat(sender, "Memory", trend.memoryTrend());
        MessageUtil.send(sender, MessageUtil.info("&7" + trend.overallSummary()));
        MessageUtil.sendFooter(sender);
    }

    public void sendSpikeHistory(CommandSender sender) {
        HistoryConfig config = pluginConfig.getHistory();
        MessageUtil.sendHeader(sender, "Lag Spike History");

        if (!config.isEnabled()) {
            MessageUtil.send(sender, MessageUtil.error("&cHistorical tracking is disabled in config.yml."));
            MessageUtil.sendFooter(sender);
            return;
        }

        List<LagSpikeEvent> spikes = lagSpikeHistory.getRecentSpikes();
        MessageUtil.sendStat(sender, "Recent spikes", String.valueOf(spikes.size()));
        MessageUtil.blank(sender);

        if (spikes.isEmpty()) {
            MessageUtil.send(sender, MessageUtil.info("&7No lag spikes recorded since server start."));
            MessageUtil.sendFooter(sender);
            return;
        }

        LagSpikeConfig lagSpikeConfig = pluginConfig.getLagSpike();
        MessageUtil.sendSection(sender, "Recent events (newest first)");
        List<LagSpikeEvent> newestFirst = spikes.reversed();
        int displayLimit = Math.min(10, newestFirst.size());
        for (int index = 0; index < displayLimit; index++) {
            LagSpikeEvent spike = newestFirst.get(index);
            LagSpikeSeverity severity = LagSpikeSeverity.from(spike, lagSpikeConfig);
            MessageUtil.send(sender, MessageUtil.info(String.format(
                    Locale.ROOT,
                    "&7• &f%s &7— &f%s &7— TPS %s, MSPT %s, mem %.1f%%",
                    spike.formattedDetectedAt(),
                    severity.label(),
                    ServerStats.formatTps(spike.tps()),
                    ServerStats.formatMspt(spike.mspt()),
                    spike.memoryUsagePercent()
            )));
        }
        if (newestFirst.size() > displayLimit) {
            MessageUtil.send(sender, MessageUtil.info(String.format(
                    Locale.ROOT,
                    "&7… and %d older spike(s) not shown.",
                    newestFirst.size() - displayLimit
            )));
        }
        MessageUtil.sendFooter(sender);
    }

    public void sendPerformanceHistory(CommandSender sender) {
        HistoryConfig config = pluginConfig.getHistory();
        MessageUtil.sendHeader(sender, "Performance Trends");

        if (!config.isEnabled()) {
            MessageUtil.send(sender, MessageUtil.error("&cHistorical tracking is disabled in config.yml."));
            MessageUtil.sendFooter(sender);
            return;
        }

        if (store.isEmpty()) {
            MessageUtil.send(sender, MessageUtil.info("&7No performance samples yet."));
            MessageUtil.sendFooter(sender);
            return;
        }

        int interval = config.getSampleIntervalSeconds();
        int samples15 = samplesForMinutes(15, interval);
        int samples60 = samplesForMinutes(60, interval);

        MessageUtil.sendSection(sender, "Rolling averages");
        appendRollingAverage(sender, "All tracked samples", store.averages());
        appendRollingAverage(
                sender,
                trendLabel(samples15, interval),
                store.averagesForLastSamples(samples15)
        );
        appendRollingAverage(
                sender,
                trendLabel(samples60, interval),
                store.averagesForLastSamples(samples60)
        );
        MessageUtil.blank(sender);

        PerformanceTrendAnalyzer.PerformanceTrendSummary trend =
                PerformanceTrendAnalyzer.analyze(store.snapshots());
        MessageUtil.sendSection(sender, "Change over tracked period");
        MessageUtil.sendStat(sender, "TPS", trend.tpsTrend());
        MessageUtil.sendStat(sender, "MSPT", trend.msptTrend());
        MessageUtil.sendStat(sender, "Memory", trend.memoryTrend());
        MessageUtil.blank(sender);
        MessageUtil.send(sender, MessageUtil.info("&7" + trend.overallSummary()));
        MessageUtil.sendFooter(sender);
    }

    private static int samplesForMinutes(int minutes, int intervalSeconds) {
        if (intervalSeconds <= 0) {
            return 1;
        }
        return Math.max(1, (minutes * 60) / intervalSeconds);
    }

    private static String trendLabel(int samples, int intervalSeconds) {
        long minutes = (samples * (long) intervalSeconds) / 60L;
        if (minutes < 1) {
            return String.format(Locale.ROOT, "Last %d samples", samples);
        }
        return String.format(Locale.ROOT, "Last ~%d minutes", minutes);
    }

    private static void appendRollingAverage(
            CommandSender sender,
            String windowLabel,
            PerformanceHistoryStore.PerformanceAverages averages
    ) {
        if (averages.sampleCount() == 0) {
            MessageUtil.sendStat(sender, windowLabel, "no data");
            return;
        }
        MessageUtil.send(sender, MessageUtil.info(String.format(
                Locale.ROOT,
                "&7▸ &f%s &7— TPS %s, MSPT %s, memory %s (&f%d&7 samples)",
                windowLabel,
                averages.formatTps(),
                averages.formatMspt(),
                averages.formatMemory(),
                averages.sampleCount()
        )));
    }
}
