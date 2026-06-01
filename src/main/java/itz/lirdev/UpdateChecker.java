package itz.lirdev;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import itz.lirdev.configuration.Config;
import itz.lirdev.tools.ColorParser;
import itz.lirdev.tools.Logger;
import itz.lirdev.tools.SchedulerUtil;

public class UpdateChecker implements Listener {

    private final JavaPlugin plugin;
    private final String currentVersion;
    private final String projectId;
    private final boolean enabled;

    private volatile String latestVersion = null;
    private final AtomicBoolean updateAvailable = new AtomicBoolean(false);
    private final AtomicBoolean checked = new AtomicBoolean(false);

    public UpdateChecker(JavaPlugin plugin, String currentVersion, String projectId, Config config) {
        this.plugin = plugin;
        this.currentVersion = currentVersion;
        this.projectId = projectId;
        this.enabled = config.isCheckUpdate();
    }

    public void start() {
        if (!enabled) {
            return;
        }
        Bukkit.getPluginManager().registerEvents(this, plugin);
        SchedulerUtil.runAsync(plugin, this::checkForUpdates, 40L);
    }

    public void checkForUpdates() {
        if (!enabled || checked.get()) {
            return;
        }

        HttpURLConnection connection = null;
        try {
            String apiUrl = "https://api.modrinth.com/v2/project/" + projectId + "/version";
            connection = (HttpURLConnection) URI.create(apiUrl).toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", plugin.getName() + "/" + currentVersion);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            if (connection.getResponseCode() != 200) {
                return;
            }

            try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
                JsonElement element = new JsonParser().parse(reader);
                if (!element.isJsonArray()) {
                    return;
                }

                JsonArray versions = element.getAsJsonArray();
                if (versions.size() == 0) {
                    return;
                }

                latestVersion = versions.get(0)
                        .getAsJsonObject()
                        .get("version_number")
                        .getAsString();

                if (isNewerVersion(currentVersion, latestVersion)) {
                    updateAvailable.set(true);
                    SchedulerUtil.runTaskLater(plugin, this::notifyConsoleAndOnline, 1L);
                }
            }

            checked.set(true);

        } catch (Exception e) {
            Logger.error("Update check failed: " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void notifyConsoleAndOnline() {
        if (!updateAvailable.get()) {
            return;
        }
        String message = formatMessage();
        Logger.msg(message);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOp() || player.hasPermission("lirbroadcast.alert")) {
                player.sendMessage(message);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!enabled || !updateAvailable.get()) {
            return;
        }
        Player player = event.getPlayer();
        if (!player.isOp() && !player.hasPermission("lirbroadcast.alert")) {
            return;
        }

        SchedulerUtil.runTaskLaterForPlayer(plugin, player,
                () -> player.sendMessage(formatMessage()), 40L);
    }

    private String formatMessage() {
        return ColorParser.colorize(
                "&#4ef89b[&lʟɪʀʙʀᴏᴀᴅᴄᴀꜱᴛ&#4ef89b] &f→&r &fUpdate available: &#4EF89Bv"
                + latestVersion + " &7(current: v" + currentVersion + ")"
        );
    }

    private boolean isNewerVersion(String current, String latest) {
        try {
            String[] cur = current.split("\\.");
            String[] lat = latest.split("\\.");
            int len = Math.max(cur.length, lat.length);
            for (int i = 0; i < len; i++) {
                int c = i < cur.length ? Integer.parseInt(cur[i]) : 0;
                int l = i < lat.length ? Integer.parseInt(lat[i]) : 0;
                if (l > c) {
                    return true;
                }
                if (l < c) {
                    return false;
                }
            }
        } catch (Exception ignored) {
            return !latest.equalsIgnoreCase(current);
        }
        return false;
    }
}
