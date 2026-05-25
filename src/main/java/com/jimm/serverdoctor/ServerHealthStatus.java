package com.jimm.serverdoctor;

/**
 * Overall and per-metric health snapshot for {@code /doctor status}.
 */
public record ServerHealthStatus(
        MessageUtil.StatusLevel overall,
        MessageUtil.StatusLevel tps,
        MessageUtil.StatusLevel mspt,
        MessageUtil.StatusLevel memory,
        MessageUtil.StatusLevel entities,
        MessageUtil.StatusLevel chunks,
        MessageUtil.StatusLevel lagSpike,
        int recommendationCount,
        String latestLagSpikeLine,
        String summaryMessage
) {
}
