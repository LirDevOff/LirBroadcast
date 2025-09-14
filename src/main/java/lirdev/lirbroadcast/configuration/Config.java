package lirdev.lirbroadcast.configuration;

import lirdev.lirbroadcast.utils.ColorParser;
import lirdev.lirbroadcast.utils.FileLoader;
import lombok.Getter;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

@Getter
public class Config {
    private final JavaPlugin plugin;
    private final FileConfiguration configuration;

    private final Map<String, List<String>> messages = new LinkedHashMap<>();
    private int interval;
    private boolean random;
    private boolean debug;
    private boolean checkupdate;
    private Sound sound;
    private float volume;
    private float pitch;
    private boolean soundEnabled;
    private String toggleOnMsg;
    private String toggleOffMsg;
    private String noPermissionMsg;
    private String reloadMsg;
    private String playerOnlyMsg;

    public Config(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configuration = FileLoader.getFileConfiguration("config.yml");
        load();
    }

    public void load() {

        messages.clear();
        ConfigurationSection messagesSection = configuration.getConfigurationSection("messages");
        if (messagesSection != null) {
            for (String key : messagesSection.getKeys(false)) {
                if (key.equals("toggle-on") || key.equals("toggle-off") ||
                        key.equals("no-permission") || key.equals("reload") ||
                        key.equals("player-only")) {
                    continue;
                }

                List<String> msgLines = ColorParser.colorize(messagesSection.getStringList(key));
                if (!msgLines.isEmpty()) {
                    messages.put(key, msgLines);
                }
            }
        }
        checkupdate = configuration.getBoolean("check-update", true);

        interval = configuration.getInt("interval", 60);
        random = configuration.getBoolean("random", false);
        debug = configuration.getBoolean("debug", false);
        soundEnabled = configuration.getBoolean("sound-enabled", true);

        try {
            sound = Sound.valueOf(configuration.getString("sound", "BLOCK_NOTE_BLOCK_PLING"));
        } catch (IllegalArgumentException e) {
            sound = Sound.BLOCK_NOTE_BLOCK_PLING;
            plugin.getLogger().warning("Incorrect sound name in the config, using default sound.");
        }

        volume = (float) configuration.getDouble("volume", 1.0);
        pitch = (float) configuration.getDouble("pitch", 1.0);

        toggleOnMsg = ColorParser.colorize(configuration.getString("messages.toggle-on", "Notifications have been enabled!"));
        toggleOffMsg = ColorParser.colorize(configuration.getString("messages.toggle-off", "Notifications have been disabled!"));
        noPermissionMsg = ColorParser.colorize(configuration.getString("messages.no-permission", "You do not have permission to use this command!"));
        reloadMsg = ColorParser.colorize(configuration.getString("messages.reload", "Configuration has been reloaded!"));
        playerOnlyMsg = ColorParser.colorize(configuration.getString("messages.player-only", "This command can only be used by players!"));
    }
}