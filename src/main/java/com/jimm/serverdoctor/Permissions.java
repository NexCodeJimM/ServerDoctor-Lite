package com.jimm.serverdoctor;

import org.bukkit.command.CommandSender;

public final class Permissions {

    public static final String USE = "serverdoctor.use";
    public static final String REPORT = "serverdoctor.report";
    public static final String ALERTS = "serverdoctor.alerts";
    public static final String EXPORT = "serverdoctor.export";
    public static final String RELOAD = "serverdoctor.reload";
    public static final String CHUNKS = "serverdoctor.chunks";
    public static final String TPCHUNK = "serverdoctor.tpchunk";
    public static final String CLEANUP_PREVIEW = "serverdoctor.cleanup.preview";
    public static final String CLEANUP_CONFIRM = "serverdoctor.cleanup.confirm";
    public static final String SPIKES = "serverdoctor.spikes";
    public static final String STATUS = "serverdoctor.status";
    public static final String ABOUT = "serverdoctor.about";
    public static final String UPDATE_NOTIFY = "serverdoctor.update.notify";
    public static final String UPDATE_CHECK = "serverdoctor.update.check";
    public static final String PLUGINS = "serverdoctor.plugins";
    public static final String HISTORY = "serverdoctor.history";
    public static final String HISTORY_SPIKES = "serverdoctor.history.spikes";
    public static final String HISTORY_PERFORMANCE = "serverdoctor.history.performance";
    public static final String SCHEDULE = "serverdoctor.schedule";

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

    public static boolean canTpChunk(CommandSender sender) {
        return sender.hasPermission(TPCHUNK);
    }

    public static boolean canCleanupPreview(CommandSender sender) {
        return sender.hasPermission(CLEANUP_PREVIEW);
    }

    public static boolean canCleanupConfirm(CommandSender sender) {
        return sender.hasPermission(CLEANUP_CONFIRM);
    }

    public static boolean canSpikes(CommandSender sender) {
        return sender.hasPermission(SPIKES);
    }

    public static boolean canStatus(CommandSender sender) {
        return sender.hasPermission(STATUS);
    }

    public static boolean canAbout(CommandSender sender) {
        return sender.hasPermission(ABOUT);
    }

    public static boolean canUpdateNotify(CommandSender sender) {
        return sender.hasPermission(UPDATE_NOTIFY);
    }

    public static boolean canUpdateCheck(CommandSender sender) {
        return sender.hasPermission(UPDATE_CHECK);
    }

    public static boolean canPlugins(CommandSender sender) {
        return sender.hasPermission(PLUGINS);
    }

    public static boolean canHistory(CommandSender sender) {
        return sender.hasPermission(HISTORY);
    }

    public static boolean canHistorySpikes(CommandSender sender) {
        return sender.hasPermission(HISTORY_SPIKES);
    }

    public static boolean canHistoryPerformance(CommandSender sender) {
        return sender.hasPermission(HISTORY_PERFORMANCE);
    }

    public static boolean canSchedule(CommandSender sender) {
        return sender.hasPermission(SCHEDULE);
    }

    public static boolean canRunAnyDoctorCommand(CommandSender sender) {
        return canUse(sender) || canReport(sender) || canAlerts(sender)
                || canExport(sender) || canReload(sender) || canChunks(sender) || canTpChunk(sender)
                || canCleanupPreview(sender) || canCleanupConfirm(sender) || canSpikes(sender)
                || canStatus(sender) || canAbout(sender) || canUpdateNotify(sender)
                || canUpdateCheck(sender) || canPlugins(sender) || canHistory(sender)
                || canHistorySpikes(sender) || canHistoryPerformance(sender) || canSchedule(sender);
    }
}
