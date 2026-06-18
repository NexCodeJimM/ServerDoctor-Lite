package com.jimm.serverdoctor;

import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Locale;

/**
 * Tracks a single in-memory investigation session for troubleshooting periods.
 */
public final class InvestigationSessionService {

    private static final int SAMPLE_INTERVAL_SECONDS = 30;

    private final ServerDoctorPlugin plugin;
    private final PluginConfig pluginConfig;

    private InvestigationSessionData activeSession;
    private InvestigationSessionSummary lastSummary;
    private BukkitTask sampleTask;

    public InvestigationSessionService(ServerDoctorPlugin plugin, PluginConfig pluginConfig) {
        this.plugin = plugin;
        this.pluginConfig = pluginConfig;
    }

    public void start() {
        stopSamplingTask();
        InvestigationConfig config = pluginConfig.getInvestigation();
        if (!config.isEnabled()) {
            return;
        }
        long intervalTicks = SAMPLE_INTERVAL_SECONDS * 20L;
        sampleTask = plugin.getServer().getScheduler().runTaskTimer(
                plugin,
                this::onSampleTick,
                intervalTicks,
                intervalTicks
        );
    }

    public void stop() {
        stopSamplingTask();
    }

    public void restart() {
        start();
    }

    public boolean isActive() {
        return activeSession != null;
    }

    public InvestigationSessionSummary getLastSummary() {
        return lastSummary;
    }

    public boolean startSession(CommandSender sender) {
        InvestigationConfig config = pluginConfig.getInvestigation();
        if (!config.isEnabled()) {
            MessageUtil.send(sender, MessageUtil.error("&cInvestigation sessions are disabled in config.yml."));
            return false;
        }
        if (activeSession != null) {
            MessageUtil.send(sender, MessageUtil.error(
                    "&cAn investigation is already active. Use &f/doctor investigate stop &cor &fstatus&c first."
            ));
            return false;
        }

        lastSummary = null;
        String name = sender.getName();
        activeSession = new InvestigationSessionData(System.currentTimeMillis(), name);
        recordPerformanceSample();

        MessageUtil.sendHeader(sender, "Investigation Started");
        MessageUtil.send(sender, MessageUtil.info(String.format(
                Locale.ROOT,
                "&aSession started by &f%s&a. Tracking performance, spikes, chunks, and cleanup during this period.",
                name
        )));
        MessageUtil.sendStat(sender, "Auto-stop", config.getAutoStopMinutes() + " minutes");
        MessageUtil.send(sender, MessageUtil.info("&7Use &f/doctor investigate status&7 to monitor progress."));
        MessageUtil.sendFooter(sender);

        plugin.getLogger().info(String.format(
                Locale.ROOT,
                "Investigation session started by %s (auto-stop in %d minutes).",
                name,
                config.getAutoStopMinutes()
        ));
        return true;
    }

    public boolean stopSession(CommandSender sender) {
        if (activeSession == null) {
            MessageUtil.send(sender, MessageUtil.error("&cNo active investigation session. Use &f/doctor investigate start&c."));
            return false;
        }
        InvestigationSessionSummary summary = finishSession(false);
        sendShortStopSummary(sender, summary);
        return true;
    }

    public void sendStatus(CommandSender sender) {
        MessageUtil.sendHeader(sender, "Investigation Status");
        InvestigationConfig config = pluginConfig.getInvestigation();
        MessageUtil.sendStat(sender, "Feature enabled", yesNo(config.isEnabled()));

        if (activeSession == null) {
            MessageUtil.sendStat(sender, "Session", "none active");
            if (lastSummary != null) {
                MessageUtil.sendStat(sender, "Last session", formatDuration(lastSummary.durationMillis()) + " (ended)");
                MessageUtil.sendStat(sender, "Last started by", lastSummary.getStartedBy());
                if (lastSummary.isAutoStopped()) {
                    MessageUtil.send(sender, MessageUtil.info("&7Last session ended automatically (auto-stop)."));
                }
            } else {
                MessageUtil.send(sender, MessageUtil.info("&7Start one with &f/doctor investigate start&7."));
            }
            MessageUtil.sendFooter(sender);
            return;
        }

        long durationMillis = System.currentTimeMillis() - activeSession.getStartTimeMillis();
        MessageUtil.sendStat(sender, "Session", "active");
        MessageUtil.sendStat(sender, "Started by", activeSession.getStartedBy());
        MessageUtil.sendStat(sender, "Duration", formatDuration(durationMillis));
        MessageUtil.blank(sender);
        MessageUtil.sendSection(sender, "Tracked so far");
        sendLiveCounts(sender, activeSession);
        MessageUtil.sendFooter(sender);
    }

    public void sendSummary(CommandSender sender) {
        if (activeSession != null) {
            sendFullSummary(sender, activeSession, System.currentTimeMillis(), false, null);
            return;
        }
        if (lastSummary != null) {
            sendCompletedSummary(sender, lastSummary);
            return;
        }
        MessageUtil.send(sender, MessageUtil.error(
                "&cNo investigation data yet. Start a session with &f/doctor investigate start&c."
        ));
    }

    public void appendExportSection(StringBuilder report) {
        if (activeSession != null) {
            appendSummaryText(
                    report,
                    activeSession,
                    System.currentTimeMillis(),
                    false,
                    buildFinalRecommendation(activeSession)
            );
            return;
        }
        if (lastSummary != null) {
            appendCompletedSummaryText(report, lastSummary);
        }
    }

    public void onLagSpike(LagSpikeEvent spike) {
        if (activeSession == null) {
            return;
        }
        activeSession.recordLagSpike(spike);
    }

    public void onChunkAnalysis(List<ChunkAnalysisResult> allResults) {
        if (activeSession == null || !pluginConfig.getInvestigation().isIncludeHeavyChunks()) {
            return;
        }
        for (ChunkAnalysisResult chunk : allResults) {
            activeSession.considerHeavyChunk(chunk);
        }
    }

    public void onCleanupPreview() {
        if (activeSession == null || !pluginConfig.getInvestigation().isIncludeCleanupEvents()) {
            return;
        }
        activeSession.recordCleanupPreview();
    }

    public void onCleanupConfirm(CleanupExecuteResult result) {
        if (activeSession == null || !pluginConfig.getInvestigation().isIncludeCleanupEvents()) {
            return;
        }
        activeSession.recordCleanupConfirm(result);
    }

    public void onRecommendationsShown(int count) {
        if (activeSession == null || !pluginConfig.getInvestigation().isIncludeRecommendations()) {
            return;
        }
        activeSession.addRecommendations(count);
    }

    private void onSampleTick() {
        if (activeSession == null) {
            return;
        }

        InvestigationConfig config = pluginConfig.getInvestigation();
        if (!config.isEnabled()) {
            return;
        }

        recordPerformanceSample();

        long elapsedMillis = System.currentTimeMillis() - activeSession.getStartTimeMillis();
        long autoStopMillis = config.getAutoStopMinutes() * 60_000L;
        if (elapsedMillis >= autoStopMillis) {
            InvestigationSessionSummary summary = finishSession(true);
            plugin.getLogger().info(String.format(
                    Locale.ROOT,
                    "Investigation session auto-stopped after %d minutes (started by %s). "
                            + "Lag spikes: %d, worst TPS: %s.",
                    config.getAutoStopMinutes(),
                    summary.getStartedBy(),
                    summary.getLagSpikeCount(),
                    ServerStats.formatTps(summary.getWorstTps())
            ));
        }
    }

    private void recordPerformanceSample() {
        if (activeSession == null) {
            return;
        }
        ServerStats stats = ServerStats.collect(plugin.getEnabledAtMillis());
        activeSession.recordPerformance(stats);
    }

    private InvestigationSessionSummary finishSession(boolean autoStopped) {
        InvestigationSessionData session = activeSession;
        long endTime = System.currentTimeMillis();
        String recommendation = buildFinalRecommendation(session);
        InvestigationSessionSummary summary = new InvestigationSessionSummary(session, endTime, autoStopped, recommendation);
        lastSummary = summary;
        activeSession = null;
        return summary;
    }

    private void sendShortStopSummary(CommandSender sender, InvestigationSessionSummary summary) {
        MessageUtil.sendHeader(sender, "Investigation Stopped");
        MessageUtil.sendStat(sender, "Duration", formatDuration(summary.durationMillis()));
        MessageUtil.sendStat(sender, "Lag spikes", String.valueOf(summary.getLagSpikeCount()));
        MessageUtil.sendStat(sender, "Worst TPS", ServerStats.formatTps(summary.getWorstTps()));
        MessageUtil.sendStat(sender, "Worst MSPT", ServerStats.formatMspt(summary.getWorstMspt()));
        MessageUtil.send(sender, MessageUtil.info("&7Full details: &f/doctor investigate summary"));
        MessageUtil.sendFooter(sender);

        plugin.getLogger().info(String.format(
                Locale.ROOT,
                "Investigation session stopped (started by %s, duration %s, spikes %d).",
                summary.getStartedBy(),
                formatDuration(summary.durationMillis()),
                summary.getLagSpikeCount()
        ));
    }

    private void sendCompletedSummary(CommandSender sender, InvestigationSessionSummary summary) {
        sendFullSummary(sender, null, summary.getEndTimeMillis(), summary.isAutoStopped(), summary);
    }

    private void sendFullSummary(
            CommandSender sender,
            InvestigationSessionData liveSession,
            long endTimeMillis,
            boolean autoStopped,
            InvestigationSessionSummary completed
    ) {
        InvestigationSessionData session = liveSession;
        boolean inProgress = session != null;
        if (!inProgress && completed != null) {
            sendFullSummaryFromCompleted(sender, completed);
            return;
        }

        MessageUtil.sendHeader(sender, inProgress ? "Investigation Summary (in progress)" : "Investigation Summary");
        if (autoStopped && !inProgress) {
            MessageUtil.send(sender, MessageUtil.info("&7This session ended automatically (auto-stop)."));
        }
        if (inProgress) {
            MessageUtil.sendStat(sender, "Started by", session.getStartedBy());
        }

        long startMillis = inProgress ? session.getStartTimeMillis() : completed.getStartTimeMillis();
        long durationMillis = endTimeMillis - startMillis;
        MessageUtil.sendStat(sender, "Duration", formatDuration(durationMillis));

        int spikes = inProgress ? session.getLagSpikeCount() : completed.getLagSpikeCount();
        double worstTps = inProgress ? session.getWorstTps() : completed.getWorstTps();
        double worstMspt = inProgress ? session.getWorstMspt() : completed.getWorstMspt();
        double peakMemory = inProgress ? session.getPeakMemoryPercent() : completed.getPeakMemoryPercent();

        MessageUtil.blank(sender);
        MessageUtil.sendSection(sender, "Performance");
        MessageUtil.sendStat(sender, "Lag spikes", String.valueOf(spikes));
        MessageUtil.sendStat(sender, "Worst TPS", ServerStats.formatTps(worstTps));
        MessageUtil.sendStat(sender, "Worst MSPT", ServerStats.formatMspt(worstMspt));
        MessageUtil.sendStat(sender, "Peak memory", String.format(Locale.ROOT, "%.1f%%", peakMemory));

        boolean hasChunk = inProgress ? session.hasHeaviestChunk() : completed.hasHeaviestChunk();
        if (hasChunk) {
            MessageUtil.blank(sender);
            MessageUtil.sendSection(sender, "Heaviest chunk (during session)");
            String world = inProgress ? session.getHeaviestChunkWorld() : completed.getHeaviestChunkWorld();
            int cx = inProgress ? session.getHeaviestChunkX() : completed.getHeaviestChunkX();
            int cz = inProgress ? session.getHeaviestChunkZ() : completed.getHeaviestChunkZ();
            int entities = inProgress ? session.getHeaviestChunkEntities() : completed.getHeaviestChunkEntities();
            double score = inProgress ? session.getHeaviestChunkScore() : completed.getHeaviestChunkScore();
            MessageUtil.sendStat(sender, "Location", world + " [" + cx + ", " + cz + "]");
            MessageUtil.sendStat(sender, "Entities", String.valueOf(entities));
            MessageUtil.sendStat(sender, "Heaviness score", String.format(Locale.ROOT, "%.2f", score));
            MessageUtil.send(sender, MessageUtil.info("&7May indicate entity pressure in that area — worth reviewing in-game."));
        }

        int previews = inProgress ? session.getCleanupPreviewCount() : completed.getCleanupPreviewCount();
        int confirms = inProgress ? session.getCleanupConfirmCount() : completed.getCleanupConfirmCount();
        int removed = inProgress ? session.getTotalEntitiesRemovedInCleanup() : completed.getTotalEntitiesRemovedInCleanup();
        int recommendations = inProgress ? session.getRecommendationCount() : completed.getRecommendationCount();

        MessageUtil.blank(sender);
        MessageUtil.sendSection(sender, "Activity");
        MessageUtil.sendStat(sender, "Cleanup previews", String.valueOf(previews));
        MessageUtil.sendStat(sender, "Cleanup confirms", String.valueOf(confirms));
        MessageUtil.sendStat(sender, "Entities removed (cleanup)", String.valueOf(removed));
        MessageUtil.sendStat(sender, "Recommendations shown", String.valueOf(recommendations));

        String finalTip = inProgress ? buildFinalRecommendation(session) : completed.getFinalRecommendation();
        MessageUtil.blank(sender);
        MessageUtil.sendSection(sender, "Final recommendation");
        MessageUtil.send(sender, MessageUtil.recommendationBullet(finalTip));
        if (inProgress) {
            MessageUtil.send(sender, MessageUtil.info("&7Session still active — run &f/doctor investigate stop &7when finished."));
        }
        MessageUtil.sendFooter(sender);
    }

    private void sendFullSummaryFromCompleted(CommandSender sender, InvestigationSessionSummary summary) {
        MessageUtil.sendHeader(sender, "Investigation Summary");
        if (summary.isAutoStopped()) {
            MessageUtil.send(sender, MessageUtil.info("&7This session ended automatically (auto-stop)."));
        }
        MessageUtil.sendStat(sender, "Started by", summary.getStartedBy());
        MessageUtil.sendStat(sender, "Duration", formatDuration(summary.durationMillis()));
        MessageUtil.blank(sender);
        MessageUtil.sendSection(sender, "Performance");
        MessageUtil.sendStat(sender, "Lag spikes", String.valueOf(summary.getLagSpikeCount()));
        MessageUtil.sendStat(sender, "Worst TPS", ServerStats.formatTps(summary.getWorstTps()));
        MessageUtil.sendStat(sender, "Worst MSPT", ServerStats.formatMspt(summary.getWorstMspt()));
        MessageUtil.sendStat(sender, "Peak memory", String.format(Locale.ROOT, "%.1f%%", summary.getPeakMemoryPercent()));

        if (summary.hasHeaviestChunk()) {
            MessageUtil.blank(sender);
            MessageUtil.sendSection(sender, "Heaviest chunk (during session)");
            MessageUtil.sendStat(
                    sender,
                    "Location",
                    summary.getHeaviestChunkWorld() + " ["
                            + summary.getHeaviestChunkX() + ", " + summary.getHeaviestChunkZ() + "]"
            );
            MessageUtil.sendStat(sender, "Entities", String.valueOf(summary.getHeaviestChunkEntities()));
            MessageUtil.sendStat(
                    sender,
                    "Heaviness score",
                    String.format(Locale.ROOT, "%.2f", summary.getHeaviestChunkScore())
            );
            MessageUtil.send(sender, MessageUtil.info("&7May indicate entity pressure in that area — worth reviewing in-game."));
        }

        MessageUtil.blank(sender);
        MessageUtil.sendSection(sender, "Activity");
        MessageUtil.sendStat(sender, "Cleanup previews", String.valueOf(summary.getCleanupPreviewCount()));
        MessageUtil.sendStat(sender, "Cleanup confirms", String.valueOf(summary.getCleanupConfirmCount()));
        MessageUtil.sendStat(sender, "Entities removed (cleanup)", String.valueOf(summary.getTotalEntitiesRemovedInCleanup()));
        MessageUtil.sendStat(sender, "Recommendations shown", String.valueOf(summary.getRecommendationCount()));

        MessageUtil.blank(sender);
        MessageUtil.sendSection(sender, "Final recommendation");
        MessageUtil.send(sender, MessageUtil.recommendationBullet(summary.getFinalRecommendation()));
        MessageUtil.sendFooter(sender);
    }

    private void sendLiveCounts(CommandSender sender, InvestigationSessionData session) {
        MessageUtil.sendStat(sender, "Performance samples", String.valueOf(session.getPerformanceSampleCount()));
        MessageUtil.sendStat(sender, "Lag spikes", String.valueOf(session.getLagSpikeCount()));
        MessageUtil.sendStat(sender, "Worst TPS", ServerStats.formatTps(session.getWorstTps()));
        MessageUtil.sendStat(sender, "Worst MSPT", ServerStats.formatMspt(session.getWorstMspt()));
        MessageUtil.sendStat(sender, "Peak memory", String.format(Locale.ROOT, "%.1f%%", session.getPeakMemoryPercent()));
        if (session.hasHeaviestChunk()) {
            MessageUtil.sendStat(
                    sender,
                    "Heaviest chunk",
                    session.getHeaviestChunkWorld() + " ["
                            + session.getHeaviestChunkX() + ", " + session.getHeaviestChunkZ() + "]"
            );
        }
        MessageUtil.sendStat(sender, "Cleanup previews", String.valueOf(session.getCleanupPreviewCount()));
        MessageUtil.sendStat(sender, "Cleanup confirms", String.valueOf(session.getCleanupConfirmCount()));
        MessageUtil.sendStat(sender, "Recommendations shown", String.valueOf(session.getRecommendationCount()));
    }

    private void appendSummaryText(
            StringBuilder report,
            InvestigationSessionData session,
            long endTimeMillis,
            boolean autoStopped,
            String finalRecommendation
    ) {
        report.append(System.lineSeparator());
        report.append("=== Investigation Session ===").append(System.lineSeparator());
        report.append(System.lineSeparator());
        if (autoStopped) {
            report.append("Note: session ended automatically (auto-stop).").append(System.lineSeparator());
        } else {
            report.append("Note: session in progress at export time.").append(System.lineSeparator());
        }
        report.append("Started by: ").append(session.getStartedBy()).append(System.lineSeparator());
        report.append("Duration: ").append(formatDuration(endTimeMillis - session.getStartTimeMillis()))
                .append(System.lineSeparator());
        report.append("Lag spikes: ").append(session.getLagSpikeCount()).append(System.lineSeparator());
        report.append("Worst TPS: ").append(ServerStats.formatTps(session.getWorstTps())).append(System.lineSeparator());
        report.append("Worst MSPT: ").append(ServerStats.formatMspt(session.getWorstMspt())).append(System.lineSeparator());
        report.append("Peak memory: ").append(String.format(Locale.ROOT, "%.1f%%", session.getPeakMemoryPercent()))
                .append(System.lineSeparator());
        if (session.hasHeaviestChunk()) {
            report.append("Heaviest chunk: ").append(session.getHeaviestChunkWorld())
                    .append(" [").append(session.getHeaviestChunkX()).append(", ")
                    .append(session.getHeaviestChunkZ()).append("] — ")
                    .append(session.getHeaviestChunkEntities()).append(" entities (score ")
                    .append(String.format(Locale.ROOT, "%.2f", session.getHeaviestChunkScore())).append(")")
                    .append(System.lineSeparator());
        }
        report.append("Cleanup previews: ").append(session.getCleanupPreviewCount()).append(System.lineSeparator());
        report.append("Cleanup confirms: ").append(session.getCleanupConfirmCount()).append(System.lineSeparator());
        report.append("Entities removed (cleanup): ").append(session.getTotalEntitiesRemovedInCleanup())
                .append(System.lineSeparator());
        report.append("Recommendations shown: ").append(session.getRecommendationCount()).append(System.lineSeparator());
        report.append("Final recommendation: ").append(finalRecommendation).append(System.lineSeparator());
    }

    private void appendCompletedSummaryText(StringBuilder report, InvestigationSessionSummary summary) {
        report.append(System.lineSeparator());
        report.append("=== Investigation Session (last completed) ===").append(System.lineSeparator());
        report.append(System.lineSeparator());
        if (summary.isAutoStopped()) {
            report.append("Session ended automatically (auto-stop).").append(System.lineSeparator());
        }
        report.append("Started by: ").append(summary.getStartedBy()).append(System.lineSeparator());
        report.append("Duration: ").append(formatDuration(summary.durationMillis())).append(System.lineSeparator());
        report.append("Lag spikes: ").append(summary.getLagSpikeCount()).append(System.lineSeparator());
        report.append("Worst TPS: ").append(ServerStats.formatTps(summary.getWorstTps())).append(System.lineSeparator());
        report.append("Worst MSPT: ").append(ServerStats.formatMspt(summary.getWorstMspt())).append(System.lineSeparator());
        report.append("Peak memory: ").append(String.format(Locale.ROOT, "%.1f%%", summary.getPeakMemoryPercent()))
                .append(System.lineSeparator());
        if (summary.hasHeaviestChunk()) {
            report.append("Heaviest chunk: ").append(summary.getHeaviestChunkWorld())
                    .append(" [").append(summary.getHeaviestChunkX()).append(", ")
                    .append(summary.getHeaviestChunkZ()).append("] — ")
                    .append(summary.getHeaviestChunkEntities()).append(" entities")
                    .append(System.lineSeparator());
        }
        report.append("Cleanup previews: ").append(summary.getCleanupPreviewCount()).append(System.lineSeparator());
        report.append("Cleanup confirms: ").append(summary.getCleanupConfirmCount()).append(System.lineSeparator());
        report.append("Entities removed (cleanup): ").append(summary.getTotalEntitiesRemovedInCleanup())
                .append(System.lineSeparator());
        report.append("Recommendations shown: ").append(summary.getRecommendationCount()).append(System.lineSeparator());
        report.append("Final recommendation: ").append(summary.getFinalRecommendation()).append(System.lineSeparator());
    }

    static String buildFinalRecommendation(InvestigationSessionData session) {
        StringBuilder parts = new StringBuilder();

        if (session.getLagSpikeCount() > 0 && session.getWorstTps() < 18.0) {
            parts.append("Repeated lag spikes with low TPS may indicate tick-time pressure worth reviewing. ");
        }
        if (session.hasHeaviestChunk() && session.getHeaviestChunkEntities() >= 80) {
            parts.append("A heavy loaded chunk was recorded — possible cause for entity-related lag in that area. ");
        }
        if (session.getPeakMemoryPercent() >= 85.0) {
            parts.append("High memory usage was observed — worth reviewing heap settings and entity counts. ");
        }
        if (session.getCleanupConfirmCount() > 0) {
            parts.append("Cleanup was used during this session; re-scan chunks if lag continues. ");
        }
        if (session.getLagSpikeCount() == 0 && session.getWorstMspt() < 50.0 && !session.hasHeaviestChunk()) {
            parts.append("Metrics stayed relatively stable — keep monitoring if players still report lag. ");
        }
        if (parts.isEmpty()) {
            parts.append("No single root cause identified — compare spikes, chunk scans, and plugin changes over time. ");
        }

        return parts.toString().trim();
    }

    static String formatDuration(long millis) {
        long totalSeconds = Math.max(0, millis / 1000);
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        if (hours > 0) {
            return String.format(Locale.ROOT, "%dh %dm %ds", hours, minutes, seconds);
        }
        if (minutes > 0) {
            return String.format(Locale.ROOT, "%dm %ds", minutes, seconds);
        }
        return String.format(Locale.ROOT, "%ds", seconds);
    }

    private void stopSamplingTask() {
        if (sampleTask != null) {
            sampleTask.cancel();
            sampleTask = null;
        }
    }

    private static String yesNo(boolean value) {
        return value ? "yes" : "no";
    }
}
