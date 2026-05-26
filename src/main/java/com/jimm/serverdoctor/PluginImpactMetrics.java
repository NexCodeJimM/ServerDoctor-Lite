package com.jimm.serverdoctor;

import java.util.ArrayList;
import java.util.List;

/**
 * Per-plugin counts collected by the impact scanner (advisory only).
 */
public final class PluginImpactMetrics implements Comparable<PluginImpactMetrics> {

    private final String name;
    private final String version;
    private final int scheduledTasks;
    private final int listeners;
    private final List<String> reviewNotes;

    public PluginImpactMetrics(
            String name,
            String version,
            int scheduledTasks,
            int listeners,
            List<String> reviewNotes
    ) {
        this.name = name;
        this.version = version;
        this.scheduledTasks = scheduledTasks;
        this.listeners = listeners;
        this.reviewNotes = List.copyOf(reviewNotes);
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public int getScheduledTasks() {
        return scheduledTasks;
    }

    public int getListeners() {
        return listeners;
    }

    public List<String> getReviewNotes() {
        return reviewNotes;
    }

    public int activityScore() {
        return scheduledTasks + listeners;
    }

    public boolean hasReviewNotes() {
        return !reviewNotes.isEmpty();
    }

    @Override
    public int compareTo(PluginImpactMetrics other) {
        int byActivity = Integer.compare(other.activityScore(), activityScore());
        if (byActivity != 0) {
            return byActivity;
        }
        return name.compareToIgnoreCase(other.name);
    }

    public static List<String> buildReviewNotes(
            String pluginName,
            int scheduledTasks,
            int listeners,
            PluginImpactScannerConfig config
    ) {
        List<String> notes = new ArrayList<>();
        if (scheduledTasks >= config.getTaskWarningThreshold()) {
            notes.add(pluginName + " — unusually high scheduled task count ("
                    + scheduledTasks + ") — possible performance contributor, worth reviewing.");
        }
        if (listeners >= config.getListenerWarningThreshold()) {
            notes.add(pluginName + " — large listener registration count ("
                    + listeners + ") — may deserve investigation.");
        }
        return notes;
    }
}
