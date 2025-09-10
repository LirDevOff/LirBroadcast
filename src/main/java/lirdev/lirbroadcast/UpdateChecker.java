package lirdev.lirbroadcast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lirdev.lirbroadcast.managers.ConfigManager;
import lirdev.lirbroadcast.utils.ColorParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

public class UpdateChecker implements Listener {
    private final ConfigManager configManager;
    private final JavaPlugin plugin;
    private final String currentVersion;
    private final String projectId;
    private final boolean checkUpdates;
    private final JsonParser jsonParser = new JsonParser();
    private String newVersion = "";
    private final Logger log = Logger.getLogger("Minecraft");

    public UpdateChecker(JavaPlugin plugin, String currentVersion, String projectId, ConfigManager configManager) {
        this.plugin = plugin;
        this.currentVersion = currentVersion;
        this.projectId = projectId;
        this.configManager = configManager;
        this.checkUpdates = configManager.isCheckupdate();

        if (this.checkUpdates) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, this::checkForUpdates, 20L);
        }
    }

    public void checkForUpdates() {
        if (!checkUpdates) {
            return;
        }

        try {
            String apiUrl = "https://api.modrinth.com/v2/project/" + projectId + "/version";
            HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", plugin.getName() + "/" + currentVersion);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            if (connection.getResponseCode() == 200) {
                try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
                    JsonElement element = jsonParser.parse(reader);
                    if (element.isJsonArray()) {
                        JsonArray versions = element.getAsJsonArray();
                        if (versions.size() > 0) {
                            newVersion = versions.get(0).getAsJsonObject()
                                    .get("version_number").getAsString();

                            if (!newVersion.equals(currentVersion)) {
                                Bukkit.getScheduler().runTask(plugin, this::notifyUpdate);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warning(ColorParser.colorize("&#FF5555Update check failed: " + e.getMessage()));
        }
    }

    private void notifyUpdate() {
        String message = ColorParser.colorize("&#4EF89B[LirBroadcast] &fUpdate available: &#4EF89Bv" + newVersion + " &7(current: v" + currentVersion + ")");
        log.info(message);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOp() || player.hasPermission("lirbroadcast.alert")) {
                player.sendMessage(message);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!checkUpdates || newVersion.isEmpty()) return;

        Player player = event.getPlayer();
        if (player.isOp() || player.hasPermission("lirbroadcast.alert")) {
            String message = ColorParser.colorize("&#4EF89B[LirBroadcast] &fUpdate available: &#4EF89Bv" + newVersion + " &7(current: v" + currentVersion + ")");
            Bukkit.getScheduler().runTaskLater(plugin, () -> player.sendMessage(message), 20L);
        }
    }
}