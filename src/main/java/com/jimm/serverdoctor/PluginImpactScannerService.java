package com.jimm.serverdoctor;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scheduler.BukkitWorker;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Scans installed plugins for task and listener counts that may deserve review.
 * Advisory only — never claims a plugin is definitely causing lag.
 */
public final class PluginImpactScannerService {

    private final ServerDoctorPlugin plugin;
    private final RecommendationService recommendationService;

    public PluginImpactScannerService(ServerDoctorPlugin plugin, RecommendationService recommendationService) {
        this.plugin = plugin;
        this.recommendationService = recommendationService;
    }

    public PluginImpactScanResult scan(PluginConfig pluginConfig) {
        PluginImpactScannerConfig config = pluginConfig.getPluginImpactScanner();
        Plugin[] installed = Bukkit.getPluginManager().getPlugins();
        int total = installed.length;

        Map<String, Integer> taskCounts = countScheduledTasksByPlugin();
        Map<String, Integer> listenerCounts = countListenersByPlugin();

        List<PluginImpactMetrics> metrics = new ArrayList<>();
        List<String> worthReview = new ArrayList<>();

        for (Plugin installedPlugin : installed) {
            String name = installedPlugin.getName();
            String version = SpigotApiCompat.pluginVersion(installedPlugin);
            int tasks = taskCounts.getOrDefault(name, 0);
            int listeners = listenerCounts.getOrDefault(name, 0);
            List<String> notes = PluginImpactMetrics.buildReviewNotes(name, tasks, listeners, config);
            worthReview.addAll(notes);
            metrics.add(new PluginImpactMetrics(name, version, tasks, listeners, notes));
        }

        metrics.sort(Comparator.comparing(PluginImpactMetrics::getName, String.CASE_INSENSITIVE_ORDER));

        PluginCountCategory category = PluginCountCategory.fromCount(total);
        PaperTimingsStatus timingsStatus = detectPaperTimings();

        addStackReviewNotes(worthReview, total, category);

        List<String> recommendations = recommendationService.forPluginImpact(
                pluginConfig,
                total,
                category,
                timingsStatus,
                metrics,
                config
        );

        return new PluginImpactScanResult(
                total,
                category,
                timingsStatus,
                metrics,
                worthReview,
                recommendations
        );
    }

    private static void addStackReviewNotes(
            List<String> worthReview,
            int total,
            PluginCountCategory category
    ) {
        if (category == PluginCountCategory.VERY_LARGE) {
            worthReview.add(
                    "Very large plugin stack (" + total + " plugins) — may deserve investigation during performance issues."
            );
        } else if (category == PluginCountCategory.HEAVY) {
            worthReview.add(
                    "Heavy plugin stack (" + total + " plugins) — worth reviewing when troubleshooting lag."
            );
        }
    }

    private Map<String, Integer> countScheduledTasksByPlugin() {
        Map<String, Integer> counts = new HashMap<>();
        try {
            for (BukkitTask task : Bukkit.getScheduler().getPendingTasks()) {
                Plugin owner = task.getOwner();
                if (owner != null && owner.isEnabled()) {
                    increment(counts, owner.getName());
                }
            }
            for (BukkitWorker worker : Bukkit.getScheduler().getActiveWorkers()) {
                Plugin owner = worker.getOwner();
                if (owner != null && owner.isEnabled()) {
                    increment(counts, owner.getName());
                }
            }
        } catch (Exception exception) {
            plugin.getLogger().fine("Could not count all scheduled tasks: " + exception.getMessage());
        }
        return counts;
    }

    private static Map<String, Integer> countListenersByPlugin() {
        Map<String, Integer> counts = new HashMap<>();
        try {
            for (HandlerList handlerList : HandlerList.getHandlerLists()) {
                for (RegisteredListener listener : handlerList.getRegisteredListeners()) {
                    Plugin owner = listener.getPlugin();
                    if (owner != null && owner.isEnabled()) {
                        increment(counts, owner.getName());
                    }
                }
            }
        } catch (Exception ignored) {
            // Best-effort only
        }
        return counts;
    }

    private static void increment(Map<String, Integer> counts, String pluginName) {
        counts.merge(pluginName, 1, Integer::sum);
    }

    private PaperTimingsStatus detectPaperTimings() {
        File serverRoot = resolveServerRoot();
        if (serverRoot == null) {
            return PaperTimingsStatus.NOT_DETECTABLE;
        }

        File paperGlobal = new File(serverRoot, "config/paper-global.yml");
        if (!paperGlobal.isFile()) {
            return PaperTimingsStatus.NOT_DETECTABLE;
        }

        try {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(paperGlobal);
            if (yaml.contains("timings.enabled")) {
                return yaml.getBoolean("timings.enabled")
                        ? PaperTimingsStatus.ENABLED
                        : PaperTimingsStatus.DISABLED;
            }
            if (yaml.contains("timings")) {
                Object timings = yaml.get("timings");
                if (timings instanceof Boolean enabled) {
                    return enabled ? PaperTimingsStatus.ENABLED : PaperTimingsStatus.DISABLED;
                }
            }
            return PaperTimingsStatus.UNKNOWN;
        } catch (Exception exception) {
            plugin.getLogger().fine("Could not read paper-global.yml: " + exception.getMessage());
            return PaperTimingsStatus.UNKNOWN;
        }
    }

    private File resolveServerRoot() {
        File dataFolder = plugin.getDataFolder();
        if (dataFolder == null) {
            return null;
        }
        File pluginsFolder = dataFolder.getParentFile();
        if (pluginsFolder == null) {
            return null;
        }
        File serverRoot = pluginsFolder.getParentFile();
        return serverRoot != null ? serverRoot : pluginsFolder;
    }

    public void sendReport(
            CommandSender sender,
            PluginConfig pluginConfig,
            PluginImpactScanResult result,
            PluginImpactScannerConfig config
    ) {
        MessageUtil.sendHeader(sender, "Plugin Impact Scanner");
        MessageUtil.sendStat(sender, "Installed plugins", String.format(Locale.ROOT, "%,d", result.getTotalPlugins()));
        MessageUtil.sendStat(
                sender,
                "Stack category",
                result.getCountCategory().label() + " — " + result.getCountCategory().description()
        );
        MessageUtil.sendStat(sender, "Paper timings", result.getTimingsStatus().label());
        MessageUtil.blank(sender);

        int totalTasks = result.getPlugins().stream().mapToInt(PluginImpactMetrics::getScheduledTasks).sum();
        int totalListeners = result.getPlugins().stream().mapToInt(PluginImpactMetrics::getListeners).sum();
        MessageUtil.sendSection(sender, "Counts (approximate)");
        MessageUtil.sendStat(sender, "Scheduled tasks", formatCountNote(totalTasks, "pending + active worker tasks"));
        MessageUtil.sendStat(sender, "Event listeners", formatCountNote(totalListeners, "registered handlers"));
        MessageUtil.blank(sender);

        if (config.isShowPluginList()) {
            MessageUtil.sendSection(sender, "Installed plugin names");
            for (PluginImpactMetrics metrics : result.getPlugins()) {
                MessageUtil.send(sender, MessageUtil.info(String.format(
                        Locale.ROOT,
                        "&7• &f%s &7v%s",
                        metrics.getName(),
                        metrics.getVersion()
                )));
            }
            MessageUtil.blank(sender);
        }

        MessageUtil.sendSection(sender, "Per-plugin activity");
        for (PluginImpactMetrics metrics : result.pluginsSortedByActivity()) {
            if (metrics.getScheduledTasks() == 0 && metrics.getListeners() == 0) {
                continue;
            }
            MessageUtil.send(sender, MessageUtil.info(String.format(
                    Locale.ROOT,
                    "&7• &f%s &7— tasks: &f%d&7, listeners: &f%d",
                    metrics.getName(),
                    metrics.getScheduledTasks(),
                    metrics.getListeners()
            )));
        }
        boolean anyActivity = result.getPlugins().stream()
                .anyMatch(metrics -> metrics.getScheduledTasks() > 0 || metrics.getListeners() > 0);
        if (!anyActivity) {
            MessageUtil.send(sender, MessageUtil.info("&7No pending tasks or registered listeners were counted."));
        }
        MessageUtil.blank(sender);

        if (!result.getWorthReviewNotes().isEmpty()) {
            MessageUtil.sendSection(sender, "Worth reviewing");
            Set<String> uniqueNotes = new LinkedHashSet<>(result.getWorthReviewNotes());
            for (String note : uniqueNotes) {
                MessageUtil.send(sender, MessageUtil.warningBullet(note));
            }
            MessageUtil.blank(sender);
        } else {
            MessageUtil.sendSection(sender, "Worth reviewing");
            MessageUtil.send(sender, MessageUtil.info("&7No plugins stood out by task/listener thresholds or stack size."));
            MessageUtil.blank(sender);
        }

        if (recommendationService.isEnabled(pluginConfig)) {
            List<String> tips = result.getRecommendations();
            if (!tips.isEmpty()) {
                MessageUtil.sendSection(sender, "Recommendations");
                for (String tip : tips) {
                    MessageUtil.send(sender, MessageUtil.recommendationBullet(tip));
                }
                MessageUtil.blank(sender);
            }
        }

        MessageUtil.send(sender, MessageUtil.info(
                "&7Counts are approximate snapshots — they do not prove a plugin is causing lag."
        ));
        MessageUtil.sendFooter(sender);
    }

    private static String formatCountNote(int count, String detail) {
        return String.format(Locale.ROOT, "%,d (%s)", count, detail);
    }
}
