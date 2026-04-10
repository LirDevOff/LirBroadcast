package itz.lirdev.configuration;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import itz.lirdev.tools.FileLoader;

public class Config {

    private final JavaPlugin plugin;
    private FileConfiguration configuration;
    private final FileLoader fileLoader;

    private boolean debug;
    private boolean checkupdate;
    private long interval;
    private boolean random;

    private String prefix;
    private String toggleOnMessage;
    private String toggleOffMessage;
    private String noPermissionMessage;
    private String reloadMessage;
    private String playerOnlyMessage;
    private String announceSentMessage;

    public Config(JavaPlugin plugin) {
        this.plugin = plugin;
        this.fileLoader = new FileLoader();
        this.configuration = fileLoader.getFileConfiguration("config.yml");
        load();
    }

    public void load() {
        this.configuration = fileLoader.getFileConfiguration("config.yml");

        checkupdate = configuration.getBoolean("check-update", true);
        debug = configuration.getBoolean("debug", false);
        interval = configuration.getLong("interval", 120);
        random = configuration.getBoolean("random", true);

        prefix = configuration.getString("messages.prefix", "&#4EF89B[Notifications]");
        toggleOnMessage = configuration.getString("messages.toggle-on", "Notifications have been enabled!");
        toggleOffMessage = configuration.getString("messages.toggle-off", "Notifications have been disabled!");
        noPermissionMessage = configuration.getString("messages.no-permission", "You don't have permission to use this command!");
        reloadMessage = configuration.getString("messages.reload", "Configuration has been reloaded!");
        playerOnlyMessage = configuration.getString("messages.player-only", "This command can only be used by players!");
        announceSentMessage = configuration.getString("messages.announce-sent", "Announcement sent!");
    }

    public FileConfiguration getConfig() {
        return configuration;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isCheckUpdate() {
        return checkupdate;
    }

    public long getInterval() {
        return interval;
    }

    public boolean isRandom() {
        return random;
    }

    public String getToggleOnMessage() {
        return toggleOnMessage;
    }

    public String getToggleOffMessage() {
        return toggleOffMessage;
    }

    public String getNoPermissionMessage() {
        return noPermissionMessage;
    }

    public String getReloadMessage() {
        return reloadMessage;
    }

    public String getPlayerOnlyMessage() {
        return playerOnlyMessage;
    }

    public String getAnnounceSentMessage() {
        return announceSentMessage;
    }

    public String getPrefix() {
        return prefix;
    }

}
