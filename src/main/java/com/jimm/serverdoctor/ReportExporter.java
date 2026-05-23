package com.jimm.serverdoctor;

import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class ReportExporter {

    private static final DateTimeFormatter FILE_NAME_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
    private static final DateTimeFormatter REPORT_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ReportExporter() {
    }

    public static String export(ServerDoctorPlugin plugin, ServerStats stats, PluginConfig config)
            throws IOException {
        File reportsFolder = new File(plugin.getDataFolder(), "reports");
        if (!reportsFolder.exists() && !reportsFolder.mkdirs()) {
            throw new IOException("Could not create reports folder: " + reportsFolder.getAbsolutePath());
        }

        String filename = "server-doctor-report-" + LocalDateTime.now().format(FILE_NAME_TIME) + ".txt";
        File reportFile = new File(reportsFolder, filename);

        String content = buildReportText(stats, config);
        Files.writeString(reportFile.toPath(), content, StandardCharsets.UTF_8);

        return filename;
    }

    private static String buildReportText(ServerStats stats, PluginConfig config) {
        StringBuilder report = new StringBuilder();

        report.append("=== ServerDoctor Export ===").append(System.lineSeparator());
        report.append(System.lineSeparator());
        report.append("Date/Time: ").append(LocalDateTime.now().format(REPORT_TIME)).append(System.lineSeparator());
        report.append("Server version: ").append(Bukkit.getVersion()).append(System.lineSeparator());
        report.append("Bukkit version: ").append(Bukkit.getBukkitVersion()).append(System.lineSeparator());
        report.append(System.lineSeparator());
        report.append("--- Server snapshot ---").append(System.lineSeparator());
        report.append("Online players: ").append(stats.onlinePlayers).append(" / ").append(stats.maxPlayers).append(System.lineSeparator());
        report.append("Memory usage: ").append(String.format("%.1f%%", stats.memoryUsagePercent))
                .append(" (").append(ServerStats.formatBytes(stats.usedBytes))
                .append(" / ").append(ServerStats.formatBytes(stats.maxBytes)).append(")").append(System.lineSeparator());
        report.append("Loaded worlds: ").append(stats.worldNames).append(System.lineSeparator());
        report.append("Loaded chunks: ").append(stats.loadedChunkCount).append(System.lineSeparator());
        report.append("Total entities: ").append(stats.entityCount).append(System.lineSeparator());
        report.append("TPS (1m avg): ").append(ServerStats.formatTps(stats.currentTps)).append(System.lineSeparator());
        report.append("MSPT: ").append(ServerStats.formatMspt(stats.mspt)).append(System.lineSeparator());
        report.append("Plugin uptime: ").append(stats.uptime).append(System.lineSeparator());
        report.append(System.lineSeparator());
        report.append("--- Config thresholds ---").append(System.lineSeparator());
        report.append("memory-warning-percent: ").append(config.getMemoryWarningPercent()).append(System.lineSeparator());
        report.append("entity-warning-limit: ").append(config.getEntityWarningLimit()).append(System.lineSeparator());
        report.append("loaded-chunk-warning-limit: ").append(config.getLoadedChunkWarningLimit()).append(System.lineSeparator());
        report.append("tps-warning-threshold: ").append(config.getTpsWarningThreshold()).append(System.lineSeparator());
        report.append("mspt-warning-threshold: ").append(config.getMsptWarningThreshold()).append(System.lineSeparator());
        report.append("alerts-enabled: ").append(config.isAlertsEnabled()).append(System.lineSeparator());
        report.append("alert-check-interval-seconds: ").append(config.getAlertCheckIntervalSeconds()).append(System.lineSeparator());
        report.append(System.lineSeparator());
        report.append("--- Current warnings ---").append(System.lineSeparator());

        List<AlertType> activeAlerts = HealthChecker.findActiveAlerts(stats, config);
        if (activeAlerts.isEmpty()) {
            report.append("No major issues detected.").append(System.lineSeparator());
        } else {
            for (AlertType type : activeAlerts) {
                report.append("- ").append(HealthChecker.formatReportWarning(type, stats, config))
                        .append(System.lineSeparator());
            }
        }

        return report.toString();
    }
}
