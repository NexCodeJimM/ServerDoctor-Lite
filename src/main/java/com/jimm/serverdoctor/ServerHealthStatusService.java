package com.jimm.serverdoctor;

import java.util.List;
import java.util.Locale;

/**
 * Calculates beginner-friendly overall server health for {@code /doctor status}.
 */
public final class ServerHealthStatusService {

    private ServerHealthStatusService() {
    }

    public static ServerHealthStatus evaluate(
            ServerStats stats,
            PluginConfig config,
            LagSpikeHistory lagSpikeHistory,
            RecommendationService recommendationService
    ) {
        List<AlertType> activeAlerts = HealthChecker.findActiveAlerts(stats, config);
        boolean recentLagSpike = hasRecentLagSpike(lagSpikeHistory, config);

        MessageUtil.StatusLevel tps = metricStatus(AlertType.TPS, activeAlerts);
        MessageUtil.StatusLevel mspt = metricStatus(AlertType.MSPT, activeAlerts);
        MessageUtil.StatusLevel memory = metricStatus(AlertType.MEMORY, activeAlerts);
        MessageUtil.StatusLevel entities = metricStatus(AlertType.ENTITIES, activeAlerts);
        MessageUtil.StatusLevel chunks = metricStatus(AlertType.CHUNKS, activeAlerts);
        MessageUtil.StatusLevel lagSpike = recentLagSpike
                ? MessageUtil.StatusLevel.WARNING
                : MessageUtil.StatusLevel.GOOD;

        MessageUtil.StatusLevel overall = calculateOverall(activeAlerts, recentLagSpike);
        int recommendationCount = countRecommendations(stats, config, recommendationService);
        String latestLagSpikeLine = formatLatestLagSpikeLine(lagSpikeHistory, config);
        String summaryMessage = buildSummaryMessage(overall, activeAlerts, recentLagSpike, recommendationCount);

        return new ServerHealthStatus(
                overall,
                tps,
                mspt,
                memory,
                entities,
                chunks,
                lagSpike,
                recommendationCount,
                latestLagSpikeLine,
                summaryMessage
        );
    }

    private static boolean hasRecentLagSpike(LagSpikeHistory lagSpikeHistory, PluginConfig config) {
        return config.getLagSpike().isEnabled() && lagSpikeHistory.hasLatestSpike();
    }

    private static MessageUtil.StatusLevel metricStatus(AlertType type, List<AlertType> activeAlerts) {
        return activeAlerts.contains(type) ? MessageUtil.StatusLevel.WARNING : MessageUtil.StatusLevel.GOOD;
    }

    private static MessageUtil.StatusLevel calculateOverall(List<AlertType> activeAlerts, boolean recentLagSpike) {
        int issueCount = activeAlerts.size() + (recentLagSpike ? 1 : 0);

        if (issueCount == 0) {
            return MessageUtil.StatusLevel.GOOD;
        }

        boolean tpsAndMsptBad = activeAlerts.contains(AlertType.TPS) && activeAlerts.contains(AlertType.MSPT);
        if (issueCount >= 3 || tpsAndMsptBad) {
            return MessageUtil.StatusLevel.CRITICAL;
        }

        return MessageUtil.StatusLevel.WARNING;
    }

    private static int countRecommendations(
            ServerStats stats,
            PluginConfig config,
            RecommendationService recommendationService
    ) {
        if (!recommendationService.isEnabled(config)) {
            return 0;
        }
        return recommendationService.forHealthReport(stats, config).size();
    }

    private static String formatLatestLagSpikeLine(LagSpikeHistory lagSpikeHistory, PluginConfig config) {
        if (!config.getLagSpike().isEnabled()) {
            return "Lag spike detection is disabled.";
        }
        if (!lagSpikeHistory.hasLatestSpike()) {
            return "No lag spikes recorded since server start.";
        }

        LagSpikeEvent latest = lagSpikeHistory.getLatestSpike();
        return String.format(
                Locale.ROOT,
                "%s (%s) — TPS %s, MSPT %s",
                latest.formattedDetectedAt(),
                latest.triggerSummary(),
                ServerStats.formatTps(latest.tps()),
                ServerStats.formatMspt(latest.mspt())
        );
    }

    private static String buildSummaryMessage(
            MessageUtil.StatusLevel overall,
            List<AlertType> activeAlerts,
            boolean recentLagSpike,
            int recommendationCount
    ) {
        return switch (overall) {
            case GOOD -> "Server health looks good right now.";
            case WARNING -> {
                int issues = activeAlerts.size() + (recentLagSpike ? 1 : 0);
                String base = issues + " area(s) need attention";
                if (recommendationCount > 0) {
                    yield base + " — " + recommendationCount + " recommendation(s) available.";
                }
                yield base + ". Run /doctor report for details.";
            }
            case CRITICAL -> {
                if (recommendationCount > 0) {
                    yield "Multiple serious issues detected — " + recommendationCount
                            + " recommendation(s). Run /doctor report immediately.";
                }
                yield "Multiple serious issues detected — run /doctor report immediately.";
            }
        };
    }
}
