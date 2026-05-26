package com.jimm.serverdoctor;

import java.util.List;
import java.util.Locale;

/**
 * Compares older and newer history samples for a simple trend summary.
 */
public final class PerformanceTrendAnalyzer {

    private PerformanceTrendAnalyzer() {
    }

    public static PerformanceTrendSummary analyze(List<PerformanceSnapshot> snapshots) {
        if (snapshots.size() < 4) {
            return new PerformanceTrendSummary(
                    "Not enough samples yet",
                    "Not enough samples yet",
                    "Not enough samples yet",
                    "Collect more history samples for trend analysis."
            );
        }

        int midpoint = snapshots.size() / 2;
        List<PerformanceSnapshot> older = snapshots.subList(0, midpoint);
        List<PerformanceSnapshot> newer = snapshots.subList(midpoint, snapshots.size());

        PerformanceHistoryStore.PerformanceAverages olderAvg = averageOf(older);
        PerformanceHistoryStore.PerformanceAverages newerAvg = averageOf(newer);

        String tpsTrend = describeHigherIsBetter(newerAvg.averageTps(), olderAvg.averageTps(), 0.3, "TPS");
        String msptTrend = describeLowerIsBetter(newerAvg.averageMspt(), olderAvg.averageMspt(), 1.0, "MSPT");
        String memoryTrend = describeLowerIsBetter(
                newerAvg.averageMemoryPercent(),
                olderAvg.averageMemoryPercent(),
                2.0,
                "Memory"
        );

        int improved = 0;
        int degraded = 0;
        if (tpsTrend.contains("improved")) {
            improved++;
        } else if (tpsTrend.contains("degraded")) {
            degraded++;
        }
        if (msptTrend.contains("improved")) {
            improved++;
        } else if (msptTrend.contains("degraded")) {
            degraded++;
        }
        if (memoryTrend.contains("improved")) {
            improved++;
        } else if (memoryTrend.contains("degraded")) {
            degraded++;
        }

        String overall;
        if (improved > degraded) {
            overall = "Performance appears to have improved compared to earlier samples.";
        } else if (degraded > improved) {
            overall = "Performance appears to have degraded compared to earlier samples.";
        } else {
            overall = "Performance looks mostly stable across tracked samples.";
        }

        return new PerformanceTrendSummary(tpsTrend, msptTrend, memoryTrend, overall);
    }

    private static PerformanceHistoryStore.PerformanceAverages averageOf(List<PerformanceSnapshot> snapshots) {
        if (snapshots.isEmpty()) {
            return PerformanceHistoryStore.PerformanceAverages.empty();
        }
        double tpsSum = 0;
        double msptSum = 0;
        double memorySum = 0;
        long chunkSum = 0;
        long entitySum = 0;
        for (PerformanceSnapshot snapshot : snapshots) {
            tpsSum += snapshot.tps();
            msptSum += snapshot.mspt();
            memorySum += snapshot.memoryUsagePercent();
            chunkSum += snapshot.loadedChunkCount();
            entitySum += snapshot.entityCount();
        }
        int count = snapshots.size();
        return new PerformanceHistoryStore.PerformanceAverages(
                tpsSum / count,
                msptSum / count,
                memorySum / count,
                chunkSum / (double) count,
                entitySum / (double) count,
                count
        );
    }

    private static String describeHigherIsBetter(double newer, double older, double threshold, String metric) {
        if (newer > older + threshold) {
            return metric + " improved";
        }
        if (newer < older - threshold) {
            return metric + " degraded";
        }
        return metric + " stable";
    }

    private static String describeLowerIsBetter(double newer, double older, double threshold, String metric) {
        if (newer < older - threshold) {
            return metric + " improved";
        }
        if (newer > older + threshold) {
            return metric + " degraded";
        }
        return metric + " stable";
    }

    public record PerformanceTrendSummary(
            String tpsTrend,
            String msptTrend,
            String memoryTrend,
            String overallSummary
    ) {
        public String rollingWindowLabel(int samples, int intervalSeconds) {
            long minutes = (samples * (long) intervalSeconds) / 60L;
            if (minutes < 1) {
                return String.format(Locale.ROOT, "last %d samples", samples);
            }
            return String.format(Locale.ROOT, "last ~%d min", minutes);
        }
    }
}
