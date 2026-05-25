package com.jimm.serverdoctor;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

/**
 * Checks SpigotMC for a newer ServerDoctor Lite release and notifies staff.
 * Network requests run off the main thread; notifications run on the main thread.
 */
public final class UpdateCheckerService implements Listener {

    private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(10);

    private final ServerDoctorPlugin plugin;
    private final PluginConfig pluginConfig;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(HTTP_TIMEOUT)
            .build();

    private volatile UpdateCheckResult lastResult = UpdateCheckResult.notChecked();
    private final AtomicBoolean checkInProgress = new AtomicBoolean(false);

    public UpdateCheckerService(ServerDoctorPlugin plugin, PluginConfig pluginConfig) {
        this.plugin = plugin;
        this.pluginConfig = pluginConfig;
    }

    public void onEnable() {
        UpdateCheckerConfig config = pluginConfig.getUpdateChecker();
        if (!config.isEnabled()) {
            return;
        }
        if (!config.isConfigured()) {
            plugin.getLogger().warning(
                    "Update checker is enabled but update-checker.spigot-resource-id is 0 — "
                            + "not configured yet. Set it to your SpigotMC resource ID (ServerDoctor: "
                            + UpdateCheckerConfig.DEFAULT_RESOURCE_ID + ")."
            );
            return;
        }
        if (config.isCheckOnStartup()) {
            runCheckAsync(false, null);
        }
    }

    public void onReload() {
        onEnable();
    }

    public UpdateCheckResult getLastResult() {
        return lastResult;
    }

    public boolean isCheckInProgress() {
        return checkInProgress.get();
    }

    public String currentVersion() {
        return PluginAbout.version(plugin);
    }

    public void runCheckAsync(boolean manual, CommandSender notifySender) {
        UpdateCheckerConfig config = pluginConfig.getUpdateChecker();
        if (!config.isEnabled() || !config.isConfigured()) {
            if (manual && notifySender != null) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        MessageUtil.send(notifySender, MessageUtil.error(
                                "&cUpdate checker is not configured. Set &fupdate-checker.spigot-resource-id &cin config.yml."
                        ))
                );
            }
            return;
        }
        if (!checkInProgress.compareAndSet(false, true)) {
            if (manual && notifySender != null) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        MessageUtil.send(notifySender, MessageUtil.info("&eAn update check is already running."))
                );
            }
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            UpdateCheckResult result;
            try {
                String remoteVersion = fetchLatestVersion(config);
                if (remoteVersion == null || remoteVersion.isBlank()) {
                    result = UpdateCheckResult.failed("SpigotMC returned an empty version response.");
                } else if (VersionUtil.isNewer(remoteVersion, currentVersion())) {
                    result = UpdateCheckResult.updateAvailable(remoteVersion.trim());
                } else {
                    result = UpdateCheckResult.upToDate(remoteVersion.trim());
                }
            } catch (Exception exception) {
                plugin.getLogger().log(Level.WARNING, "SpigotMC update check failed", exception);
                result = UpdateCheckResult.failed("Could not reach SpigotMC. Try again later.");
            }

            UpdateCheckResult finalResult = result;
            Bukkit.getScheduler().runTask(plugin, () -> {
                lastResult = finalResult;
                checkInProgress.set(false);

                if (finalResult.isUpdateAvailable()) {
                    logUpdateAvailable(finalResult.getRemoteVersion());
                    notifyOnlineStaff();
                } else if (manual) {
                    plugin.getLogger().info("ServerDoctor update check: " + finalResult.getMessage());
                }

                if (notifySender != null) {
                    sendUpdateStatus(notifySender, true);
                }
            });
        });
    }

    private String fetchLatestVersion(UpdateCheckerConfig config) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.updateApiUrl()))
                .timeout(HTTP_TIMEOUT)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("HTTP " + response.statusCode());
        }
        return response.body() == null ? "" : response.body().trim();
    }

    private void logUpdateAvailable(String remoteVersion) {
        UpdateCheckerConfig config = pluginConfig.getUpdateChecker();
        plugin.getLogger().info(String.format(
                Locale.ROOT,
                "A newer ServerDoctor Lite version is available: %s (you have %s). Download: %s",
                remoteVersion,
                currentVersion(),
                config.resourcePageUrl()
        ));
    }

    private void notifyOnlineStaff() {
        UpdateCheckerConfig config = pluginConfig.getUpdateChecker();
        if (!config.isEnabled() || !lastResult.isUpdateAvailable()) {
            return;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(Permissions.UPDATE_NOTIFY)) {
                sendUpdateNotification(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        UpdateCheckerConfig config = pluginConfig.getUpdateChecker();
        if (!config.isEnabled() || !config.isNotifyOpsOnJoin() || !lastResult.isUpdateAvailable()) {
            return;
        }
        Player player = event.getPlayer();
        if (player.hasPermission(Permissions.UPDATE_NOTIFY)) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    sendUpdateNotification(player);
                }
            }, 40L);
        }
    }

    public void sendUpdateStatus(CommandSender sender, boolean afterManualCheck) {
        UpdateCheckerConfig config = pluginConfig.getUpdateChecker();
        String current = currentVersion();

        MessageUtil.sendHeader(sender, "Update");
        MessageUtil.sendStat(sender, "Current version", current);

        if (!config.isEnabled()) {
            MessageUtil.sendStat(sender, "Checker", "disabled in config");
            MessageUtil.sendFooter(sender);
            return;
        }

        if (!config.isConfigured()) {
            MessageUtil.sendStat(sender, "Checker", "not configured (resource ID is 0)");
            MessageUtil.send(sender, MessageUtil.info(
                    "&7Set &fupdate-checker.spigot-resource-id &7to &f"
                            + UpdateCheckerConfig.DEFAULT_RESOURCE_ID
                            + " &7in config.yml, then &f/doctor reload&7."
            ));
            MessageUtil.sendFooter(sender);
            return;
        }

        if (isCheckInProgress()) {
            MessageUtil.sendStat(sender, "Latest known", "checking SpigotMC...");
            MessageUtil.sendFooter(sender);
            return;
        }

        UpdateCheckResult result = lastResult;
        String latestDisplay = switch (result.getState()) {
            case NOT_CHECKED -> "not checked yet";
            case CHECK_FAILED -> "unknown (" + result.getMessage() + ")";
            default -> result.getRemoteVersion() != null ? result.getRemoteVersion() : "unknown";
        };
        MessageUtil.sendStat(sender, "Latest known", latestDisplay);

        String availability = switch (result.getState()) {
            case NOT_CHECKED -> afterManualCheck ? result.getMessage() : "not checked — use /doctor update check";
            case UP_TO_DATE -> "Up to date";
            case UPDATE_AVAILABLE -> "Update available";
            case CHECK_FAILED -> "Check failed";
        };
        MessageUtil.sendStat(sender, "Status", availability);

        if (result.isUpdateAvailable()) {
            MessageUtil.send(sender, MessageUtil.info(
                    "&eVersion &f" + result.getRemoteVersion() + " &eis available on SpigotMC."
            ));
        } else if (result.getState() == UpdateCheckResult.State.UP_TO_DATE && afterManualCheck) {
            MessageUtil.send(sender, MessageUtil.info("&7" + result.getMessage()));
        }

        MessageUtil.sendStat(sender, "SpigotMC page", config.resourcePageUrl());
        MessageUtil.send(sender, clickableResourceLink(config.resourcePageUrl()));
        MessageUtil.sendFooter(sender);
    }

    private static Component clickableResourceLink(String url) {
        return MessageUtil.prefixed(
                Component.text("Open download page", NamedTextColor.AQUA)
                        .decorate(net.kyori.adventure.text.format.TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.openUrl(url))
        );
    }

    private void sendUpdateNotification(Player player) {
        UpdateCheckResult result = lastResult;
        if (!result.isUpdateAvailable()) {
            return;
        }
        MessageUtil.send(player, MessageUtil.info(String.format(
                Locale.ROOT,
                "&eServerDoctor Lite &f%s &eis available (you have &f%s&e). Use &f/doctor update &efor details.",
                result.getRemoteVersion(),
                currentVersion()
        )));
        UpdateCheckerConfig config = pluginConfig.getUpdateChecker();
        MessageUtil.send(player, clickableResourceLink(config.resourcePageUrl()));
    }
}
