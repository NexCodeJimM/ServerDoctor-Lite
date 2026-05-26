package com.jimm.serverdoctor;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;

import java.util.List;

public final class MessageUtil {

    private static final String DIVIDER_LINE = "────────────────────────────────";
    private static MessageConfig messages = new MessageConfig();

    private MessageUtil() {
    }

    public static void setMessages(MessageConfig messageConfig) {
        messages = messageConfig;
    }

    public enum StatusLevel {
        GOOD(NamedTextColor.GREEN, "GOOD"),
        WARNING(NamedTextColor.YELLOW, "WARNING"),
        CRITICAL(NamedTextColor.RED, "CRITICAL");

        private final NamedTextColor color;
        private final String label;

        StatusLevel(NamedTextColor color, String label) {
            this.color = color;
            this.label = label;
        }

        public NamedTextColor color() {
            return color;
        }

        public String label() {
            return label;
        }
    }

    public static void send(CommandSender sender, Component message) {
        AdventureMessages.send(sender, message);
    }

    public static void blank(CommandSender sender) {
        AdventureMessages.send(sender, Component.empty());
    }

    public static void sendHeader(CommandSender sender, String title) {
        blank(sender);
        send(sender, header(title));
        send(sender, divider());
        blank(sender);
    }

    public static void sendSection(CommandSender sender, String sectionName) {
        send(sender, section(sectionName));
    }

    public static void sendStat(CommandSender sender, String label, String value) {
        send(sender, stat(label, value));
    }

    public static void sendStat(CommandSender sender, String label, String value, StatusLevel level) {
        send(sender, stat(label, value, level));
    }

    public static void sendFooter(CommandSender sender) {
        blank(sender);
        send(sender, divider());
        blank(sender);
    }

    public static Component prefix() {
        return messages.prefix();
    }

    public static Component prefixed(Component messagePart) {
        return prefix().append(messagePart);
    }

    public static Component header(String title) {
        return prefix().append(messages.colorize("&b" + title));
    }

    public static Component divider() {
        return Component.text(DIVIDER_LINE, NamedTextColor.DARK_GRAY);
    }

    public static Component section(String sectionName) {
        return Component.text("▸ ", NamedTextColor.GOLD)
                .append(Component.text(sectionName, NamedTextColor.YELLOW));
    }

    /**
     * Chunk analyzer rank line — location is clickable when {@code teleportClickable} is true.
     */
    public static Component chunkRankHeader(int rank, ChunkAnalysisResult result, boolean teleportClickable) {
        Component header = Component.text("▸ ", NamedTextColor.GOLD)
                .append(Component.text("#" + rank + " — ", NamedTextColor.YELLOW));

        if (!teleportClickable) {
            return header.append(Component.text(
                    result.worldName + " [" + result.chunkCoordinates() + "]",
                    NamedTextColor.WHITE
            ));
        }

        return header.append(clickableChunkLocation(result));
    }

    public static Component clickableChunkLocation(ChunkAnalysisResult result) {
        String command = ChunkTeleportService.buildRunCommand(result.worldName, result.chunkX, result.chunkZ);
        String label = result.worldName + " [" + result.chunkCoordinates() + "]";

        return Component.text(label, NamedTextColor.AQUA, TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.runCommand(command))
                .hoverEvent(HoverEvent.showText(Component.text("Click to teleport to this chunk", NamedTextColor.GRAY)));
    }

    public static Component stat(String label, String value) {
        return Component.text("  ", NamedTextColor.GRAY)
                .append(Component.text(label + ": ", NamedTextColor.AQUA))
                .append(Component.text(value, NamedTextColor.WHITE));
    }

    public static Component stat(String label, String value, StatusLevel level) {
        return Component.text("  ", NamedTextColor.GRAY)
                .append(statusBadge(level))
                .append(Component.text(" ", NamedTextColor.GRAY))
                .append(Component.text(label + ": ", NamedTextColor.AQUA))
                .append(Component.text(value, NamedTextColor.WHITE));
    }

    public static Component statusBadge(StatusLevel level) {
        return Component.text("[" + level.label() + "]", level.color(), TextDecoration.BOLD);
    }

    public static Component overallStatus(StatusLevel level, String summary) {
        return statusBadge(level)
                .append(Component.text(" ", NamedTextColor.GRAY))
                .append(Component.text(summary, NamedTextColor.WHITE));
    }

    public static Component overallStatus(StatusLevel level, Component summary) {
        return statusBadge(level).append(Component.text(" ", NamedTextColor.GRAY)).append(summary);
    }

    public static Component warningBullet(String warningText) {
        return Component.text("  ", NamedTextColor.GRAY)
                .append(statusBadge(StatusLevel.WARNING))
                .append(Component.text(" ", NamedTextColor.GRAY))
                .append(Component.text(warningText, NamedTextColor.YELLOW));
    }

    public static Component recommendationBullet(String recommendationText) {
        return Component.text("  ", NamedTextColor.GRAY)
                .append(Component.text("• ", NamedTextColor.GREEN))
                .append(Component.text(recommendationText, NamedTextColor.GRAY));
    }

    public static Component permissionDenied(String permissionNode) {
        return prefixed(messages.noPermission(permissionNode));
    }

    public static Component reloadSuccess() {
        return prefixed(messages.reloadSuccess());
    }

    public static Component reportGenerated(String filename) {
        return prefixed(messages.reportGenerated(filename));
    }

    public static Component noWarnings() {
        return prefixed(messages.noWarningsText());
    }

    public static Component unknownCommand() {
        return prefixed(messages.unknownCommand());
    }

    public static Component error(String legacyMessage) {
        return prefixed(messages.colorize(legacyMessage));
    }

    public static Component info(String legacyMessage) {
        return prefixed(messages.colorize(legacyMessage));
    }

    public static Component helpEntry(String command, String description) {
        return Component.text("  ", NamedTextColor.GRAY)
                .append(Component.text(command, NamedTextColor.YELLOW))
                .append(Component.text(" — ", NamedTextColor.DARK_GRAY))
                .append(Component.text(description, NamedTextColor.GRAY));
    }

    public static Component alertBroadcast(String detail) {
        return prefix()
                .append(statusBadge(StatusLevel.WARNING))
                .append(Component.text(" ", NamedTextColor.GRAY))
                .append(Component.text(detail, NamedTextColor.YELLOW));
    }

    public static StatusLevel overallStatusFromAlerts(List<AlertType> activeAlerts) {
        if (activeAlerts.isEmpty()) {
            return StatusLevel.GOOD;
        }
        if (activeAlerts.size() >= 3) {
            return StatusLevel.CRITICAL;
        }
        return StatusLevel.WARNING;
    }

    public static StatusLevel metricStatus(AlertType type, ServerStats stats, PluginConfig config) {
        if (HealthChecker.findActiveAlerts(stats, config).contains(type)) {
            return StatusLevel.WARNING;
        }
        return StatusLevel.GOOD;
    }

    public static String formatMetricValue(AlertType type, ServerStats stats, PluginConfig config) {
        return switch (type) {
            case MEMORY -> String.format("%.1f%% (limit %.0f%%)", stats.memoryUsagePercent, config.getMemoryWarningPercent());
            case ENTITIES -> String.format("%,d (limit %,d)", stats.entityCount, config.getEntityWarningLimit());
            case CHUNKS -> String.format("%,d (limit %,d)", stats.loadedChunkCount, config.getLoadedChunkWarningLimit());
            case TPS -> String.format("%.2f (limit %.1f)", stats.currentTps, config.getTpsWarningThreshold());
            case MSPT -> String.format("%.2f ms (limit %.1f ms)", stats.mspt, config.getMsptWarningThreshold());
        };
    }
}
