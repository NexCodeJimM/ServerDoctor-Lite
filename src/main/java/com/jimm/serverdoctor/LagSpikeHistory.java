package com.jimm.serverdoctor;

import java.util.OptionalLong;

/**
 * Stores the latest detected lag spike and alert cooldown state.
 */
public final class LagSpikeHistory {

    private LagSpikeEvent latestSpike;
    private long lastAlertSentAtMillis;

    public void recordLatest(LagSpikeEvent event) {
        this.latestSpike = event;
    }

    public LagSpikeEvent getLatestSpike() {
        return latestSpike;
    }

    public boolean hasLatestSpike() {
        return latestSpike != null;
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
