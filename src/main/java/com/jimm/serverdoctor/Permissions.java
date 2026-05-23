package com.jimm.serverdoctor;

import org.bukkit.command.CommandSender;

public final class Permissions {

    public static final String USE = "serverdoctor.use";
    public static final String REPORT = "serverdoctor.report";
    public static final String ALERTS = "serverdoctor.alerts";
    public static final String EXPORT = "serverdoctor.export";
    public static final String RELOAD = "serverdoctor.reload";
    public static final String CHUNKS = "serverdoctor.chunks";

    private Permissions() {
    }

    public static boolean canUse(CommandSender sender) {
        return sender.hasPermission(USE);
    }

    public static boolean canReport(CommandSender sender) {
        return sender.hasPermission(REPORT);
    }

    public static boolean canAlerts(CommandSender sender) {
        return sender.hasPermission(ALERTS);
    }

    public static boolean canExport(CommandSender sender) {
        return sender.hasPermission(EXPORT);
    }

    public static boolean canReload(CommandSender sender) {
        return sender.hasPermission(RELOAD);
    }

    public static boolean canChunks(CommandSender sender) {
        return sender.hasPermission(CHUNKS);
    }

    public static boolean canRunAnyDoctorCommand(CommandSender sender) {
        return canUse(sender) || canReport(sender) || canAlerts(sender)
                || canExport(sender) || canReload(sender) || canChunks(sender);
    }
}
