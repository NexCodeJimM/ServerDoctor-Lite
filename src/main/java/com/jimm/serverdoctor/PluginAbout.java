package com.jimm.serverdoctor;

import org.bukkit.command.CommandSender;

/**
 * Plugin metadata shown by {@code /doctor about}.
 */
public final class PluginAbout {

    public static final String PLUGIN_NAME = "ServerDoctor";
    public static final String EDITION = "Lite";
    public static final String AUTHOR = "jimm";
    public static final String PLATFORM = "Paper 26.1.x";
    public static final String DESCRIPTION =
            "Monitor server health, find lag-heavy chunks, and get beginner-friendly recommendations.";
    /** Placeholder until a public repository URL is published. */
    public static final String PROJECT_LINK = "https://github.com/your-org/ServerDoctor (placeholder)";

    private PluginAbout() {
    }

    public static String version(ServerDoctorPlugin plugin) {
        return plugin.getPluginMeta().getVersion();
    }

    public static void sendAbout(CommandSender sender, ServerDoctorPlugin plugin) {
        MessageUtil.sendHeader(sender, "About");
        MessageUtil.sendStat(sender, "Plugin", PLUGIN_NAME + " " + EDITION);
        MessageUtil.sendStat(sender, "Version", version(plugin));
        MessageUtil.sendStat(sender, "Author", AUTHOR);
        MessageUtil.sendStat(sender, "Platform", PLATFORM);
        MessageUtil.blank(sender);
        MessageUtil.sendSection(sender, "Description");
        MessageUtil.send(sender, MessageUtil.info("&7" + DESCRIPTION));
        MessageUtil.blank(sender);
        MessageUtil.sendStat(sender, "Project", PROJECT_LINK);
        MessageUtil.sendFooter(sender);
    }
}
