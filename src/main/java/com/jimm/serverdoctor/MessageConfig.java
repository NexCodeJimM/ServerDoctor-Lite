package com.jimm.serverdoctor;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Loads customizable chat text from the {@code messages} section of config.yml.
 * <p>
 * Strings use {@code &} color codes (for example {@code &a} = green). Placeholders:
 * {@code {permission}} in no-permission, {@code {file}} in report-generated.
 */
public final class MessageConfig {

    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.legacyAmpersand();

    private String prefix;
    private String noPermission;
    private String reloadSuccess;
    private String reportGenerated;
    private String noWarnings;
    private String unknownCommand;

    public void load(FileConfiguration config) {
        prefix = config.getString("messages.prefix", "&6[ServerDoctor]&r ");
        noPermission = config.getString(
                "messages.no-permission",
                "&cYou do not have permission. Ask an admin for &f'{permission}'&c."
        );
        reloadSuccess = config.getString(
                "messages.reload-success",
                "&aConfiguration reloaded. All services restarted."
        );
        reportGenerated = config.getString(
                "messages.report-generated",
                "&aReport generated successfully: &f{file}"
        );
        noWarnings = config.getString(
                "messages.no-warnings",
                "&aNo major issues detected."
        );
        unknownCommand = config.getString(
                "messages.unknown-command",
                "&cUnknown subcommand. Use &f/doctor help &cfor available commands."
        );
    }

    public Component colorize(String text) {
        return LEGACY.deserialize(text);
    }

    public Component prefix() {
        return colorize(prefix);
    }

    public Component noPermission(String permissionNode) {
        return colorize(noPermission.replace("{permission}", permissionNode));
    }

    public Component reloadSuccess() {
        return colorize(reloadSuccess);
    }

    public Component reportGenerated(String filename) {
        return colorize(reportGenerated.replace("{file}", filename));
    }

    /** Text only (no prefix) — used next to a [GOOD] status badge. */
    public Component noWarningsText() {
        return colorize(noWarnings);
    }

    public Component unknownCommand() {
        return colorize(unknownCommand);
    }
}
