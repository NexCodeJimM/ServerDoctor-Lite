package com.jimm.serverdoctor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Result of a confirmed cleanup run.
 */
public record CleanupExecuteResult(
        LocalDateTime executedAt,
        String executedBy,
        int worldsScanned,
        int droppedItemsRemoved,
        int hostileMobsRemoved,
        int passiveMobsRemoved,
        int totalRemoved
) {

    private static final DateTimeFormatter DISPLAY_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String formattedExecutedAt() {
        return executedAt.format(DISPLAY_TIME);
    }
}
