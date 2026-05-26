package com.jimm.serverdoctor;

/**
 * Stores cleanup results and session counts for export and history commands.
 */
public final class CleanupHistory {

    private CleanupExecuteResult lastResult;
    private int cleanupCount;

    public void record(CleanupExecuteResult result) {
        this.lastResult = result;
        cleanupCount++;
    }

    public CleanupExecuteResult getLastResult() {
        return lastResult;
    }

    public boolean hasLastResult() {
        return lastResult != null;
    }

    public int getCleanupCount() {
        return cleanupCount;
    }
}
