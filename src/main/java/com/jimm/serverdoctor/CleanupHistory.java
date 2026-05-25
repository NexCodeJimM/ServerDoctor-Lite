package com.jimm.serverdoctor;

/**
 * Stores the most recent cleanup result for export and admin reference.
 */
public final class CleanupHistory {

    private CleanupExecuteResult lastResult;

    public void record(CleanupExecuteResult result) {
        this.lastResult = result;
    }

    public CleanupExecuteResult getLastResult() {
        return lastResult;
    }

    public boolean hasLastResult() {
        return lastResult != null;
    }
}
