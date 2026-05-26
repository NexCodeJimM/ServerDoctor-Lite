package com.jimm.serverdoctor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * In-memory ring buffer of recent performance snapshots.
 */
public final class PerformanceHistoryStore {

    private final List<PerformanceSnapshot> entries = new ArrayList<>();
    private int maxEntries = 1440;

    public void setMaxEntries(int maxEntries) {
        this.maxEntries = Math.max(10, maxEntries);
        trim();
    }

    public synchronized void add(PerformanceSnapshot snapshot) {
        entries.add(snapshot);
        trim();
    }

    public synchronized List<PerformanceSnapshot> snapshots() {
        return Collections.unmodifiableList(new ArrayList<>(entries));
    }

    public synchronized int size() {
        return entries.size();
    }

    public synchronized boolean isEmpty() {
        return entries.isEmpty();
    }

    public synchronized PerformanceAverages averages() {
        if (entries.isEmpty()) {
            return PerformanceAverages.empty();
        }
        double tpsSum = 0;
        double msptSum = 0;
        double memorySum = 0;
        long chunkSum = 0;
        long entitySum = 0;
        for (PerformanceSnapshot snapshot : entries) {
            tpsSum += snapshot.tps();
            msptSum += snapshot.mspt();
            memorySum += snapshot.memoryUsagePercent();
            chunkSum += snapshot.loadedChunkCount();
            entitySum += snapshot.entityCount();
        }
        int count = entries.size();
        return new PerformanceAverages(
                tpsSum / count,
                msptSum / count,
                memorySum / count,
                chunkSum / (double) count,
                entitySum / (double) count,
                count
        );
    }

    public synchronized PerformanceAverages averagesForLastSamples(int sampleCount) {
        if (entries.isEmpty() || sampleCount <= 0) {
            return PerformanceAverages.empty();
        }
        int fromIndex = Math.max(0, entries.size() - sampleCount);
        List<PerformanceSnapshot> slice = entries.subList(fromIndex, entries.size());
        double tpsSum = 0;
        double msptSum = 0;
        double memorySum = 0;
        long chunkSum = 0;
        long entitySum = 0;
        for (PerformanceSnapshot snapshot : slice) {
            tpsSum += snapshot.tps();
            msptSum += snapshot.mspt();
            memorySum += snapshot.memoryUsagePercent();
            chunkSum += snapshot.loadedChunkCount();
            entitySum += snapshot.entityCount();
        }
        int count = slice.size();
        return new PerformanceAverages(
                tpsSum / count,
                msptSum / count,
                memorySum / count,
                chunkSum / (double) count,
                entitySum / (double) count,
                count
        );
    }

    private void trim() {
        while (entries.size() > maxEntries) {
            entries.removeFirst();
        }
    }

    public record PerformanceAverages(
            double averageTps,
            double averageMspt,
            double averageMemoryPercent,
            double averageLoadedChunks,
            double averageEntities,
            int sampleCount
    ) {
        static PerformanceAverages empty() {
            return new PerformanceAverages(0, 0, 0, 0, 0, 0);
        }

        String formatTps() {
            return String.format(Locale.ROOT, "%.2f", averageTps);
        }

        String formatMspt() {
            return String.format(Locale.ROOT, "%.2f ms", averageMspt);
        }

        String formatMemory() {
            return String.format(Locale.ROOT, "%.1f%%", averageMemoryPercent);
        }
    }
}
