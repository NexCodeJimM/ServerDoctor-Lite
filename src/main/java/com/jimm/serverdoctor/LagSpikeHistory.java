package com.jimm.serverdoctor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalLong;

/**
 * Stores lag spike events and alert cooldown state.
 */
public final class LagSpikeHistory {

    private static final int MAX_RECENT_SPIKES = 50;

    private LagSpikeEvent latestSpike;
    private long lastAlertSentAtMillis;
    private final List<LagSpikeEvent> recentSpikes = new ArrayList<>();

    public void recordLatest(LagSpikeEvent event) {
        this.latestSpike = event;
        recentSpikes.add(event);
        while (recentSpikes.size() > MAX_RECENT_SPIKES) {
            recentSpikes.removeFirst();
        }
    }

    public LagSpikeEvent getLatestSpike() {
        return latestSpike;
    }

    public boolean hasLatestSpike() {
        return latestSpike != null;
    }

    public List<LagSpikeEvent> getRecentSpikes() {
        return Collections.unmodifiableList(new ArrayList<>(recentSpikes));
    }

    public int getRecentSpikeCount() {
        return recentSpikes.size();
    }

    public boolean shouldSendAlert(int cooldownSeconds) {
        if (cooldownSeconds <= 0) {
            return true;
        }
        if (lastAlertSentAtMillis == 0) {
            return true;
        }
        long elapsedMillis = System.currentTimeMillis() - lastAlertSentAtMillis;
        return elapsedMillis >= cooldownSeconds * 1000L;
    }

    public void markAlertSent() {
        lastAlertSentAtMillis = System.currentTimeMillis();
    }

    public OptionalLong remainingCooldownSeconds(int cooldownSeconds) {
        if (cooldownSeconds <= 0 || lastAlertSentAtMillis == 0) {
            return OptionalLong.empty();
        }

        long elapsedSeconds = (System.currentTimeMillis() - lastAlertSentAtMillis) / 1000;
        long remaining = cooldownSeconds - elapsedSeconds;
        if (remaining > 0) {
            return OptionalLong.of(remaining);
        }
        return OptionalLong.empty();
    }
}
