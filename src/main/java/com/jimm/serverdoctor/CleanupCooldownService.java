package com.jimm.serverdoctor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.OptionalLong;

/**
 * Prevents repeated cleanup confirm runs within a configured cooldown window.
 */
public final class CleanupCooldownService {

    private CleanupCooldownService() {
    }

    /**
     * @return seconds remaining on cooldown, or empty if confirm is allowed
     */
    public static OptionalLong remainingSeconds(CleanupHistory history, int cooldownSeconds) {
        if (cooldownSeconds <= 0) {
            return OptionalLong.empty();
        }

        CleanupExecuteResult last = history.getLastResult();
        if (last == null) {
            return OptionalLong.empty();
        }

        long elapsedSeconds = Duration.between(last.executedAt(), LocalDateTime.now()).getSeconds();
        long remaining = cooldownSeconds - elapsedSeconds;
        if (remaining > 0) {
            return OptionalLong.of(remaining);
        }
        return OptionalLong.empty();
    }

    public static String formatRemaining(long remainingSeconds) {
        long hours = remainingSeconds / 3600;
        long minutes = (remainingSeconds % 3600) / 60;
        long seconds = remainingSeconds % 60;

        if (hours > 0) {
            return String.format(Locale.ROOT, "%dh %dm %ds", hours, minutes, seconds);
        }
        if (minutes > 0) {
            return String.format(Locale.ROOT, "%dm %ds", minutes, seconds);
        }
        return seconds + "s";
    }
}
