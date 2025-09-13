package lirdev.lirbroadcast.managers;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;

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

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        reloadConfig();
    }

    public void reloadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();

        messages.clear();
        ConfigurationSection messagesSection = config.getConfigurationSection("messages");
        if (messagesSection != null) {
            for (String key : messagesSection.getKeys(false)) {
                if (key.equals("toggle-on") || key.equals("toggle-off") ||
                        key.equals("no-permission") || key.equals("reload") ||
                        key.equals("player-only")) {
                    continue;
                }

                List<String> msgLines = messagesSection.getStringList(key);
                if (msgLines != null && !msgLines.isEmpty()) {
                    messages.put(key, new ArrayList<>(msgLines));
                }
            }
        }
        checkupdate = config.getBoolean("check-update", true);

        interval = config.getInt("interval", 60);
        random = config.getBoolean("random", false);
        debug = config.getBoolean("debug", false);
        soundEnabled = config.getBoolean("sound-enabled", true);

        try {
            sound = Sound.valueOf(config.getString("sound", "BLOCK_NOTE_BLOCK_PLING"));
        } catch (IllegalArgumentException e) {
            sound = Sound.BLOCK_NOTE_BLOCK_PLING;
            plugin.getLogger().warning("Incorrect sound name in the config, using default sound.");
        }

        volume = (float) config.getDouble("volume", 1.0);
        pitch = (float) config.getDouble("pitch", 1.0);

        toggleOnMsg = config.getString("messages.toggle-on", "Notifications have been enabled!");
        toggleOffMsg = config.getString("messages.toggle-off", "Notifications have been disabled!");
        noPermissionMsg = config.getString("messages.no-permission", "You do not have permission to use this command!");
        reloadMsg = config.getString("messages.reload", "Configuration has been reloaded!");
        playerOnlyMsg = config.getString("messages.player-only", "This command can only be used by players!");
    }

    public Map<String, List<String>> getMessages() { return messages; }
    public int getInterval() { return interval; }
    public boolean isRandom() { return random; }
    public boolean isCheckupdate() { return checkupdate; }
    public Sound getSound() { return sound; }
    public float getVolume() { return volume; }
    public float getPitch() { return pitch; }
    public boolean isSoundEnabled() { return soundEnabled; }
    public String getToggleOnMsg() { return toggleOnMsg; }
    public String getToggleOffMsg() { return toggleOffMsg; }
    public String getNoPermissionMsg() { return noPermissionMsg; }
    public String getReloadMsg() { return reloadMsg; }
    public String getPlayerOnlyMsg() { return playerOnlyMsg; }
    public boolean isDebug() { return debug; }
}