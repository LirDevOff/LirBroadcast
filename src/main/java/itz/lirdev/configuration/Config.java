package itz.lirdev.configuration;

import java.io.File;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import itz.lirdev.tools.FileLoader;
import itz.lirdev.tools.TimeParser;

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
    private String announceNotFoundMessage;
    private List<String> helpMessage;

    public Config(JavaPlugin plugin) {
        this.plugin = plugin;
        this.fileLoader = new FileLoader();
        this.configuration = fileLoader.getFileConfiguration("config.yml");
        runMigration();
        readValues();
    }

    public boolean load() {
        this.configuration = fileLoader.getFileConfiguration("config.yml");
        readValues();
        return true;
    }

    private void runMigration() {
        File userFile = new File(plugin.getDataFolder(), "config.yml");
        ConfigMigrator migrator = new ConfigMigrator(userFile);
        boolean migrated = migrator.migrate(configuration);
        if (migrated) {
            this.configuration = fileLoader.getFileConfiguration("config.yml");
        }
    }

    private void readValues() {
        checkupdate = configuration.getBoolean("check-update", true);
        debug = configuration.getBoolean("debug", false);
        interval = TimeParser.toSeconds(configuration.getString("interval", "1m"));
        random = configuration.getBoolean("random", true);

        prefix = configuration.getString("messages.prefix", "&#4ef89b[&lʟɪʀʙʀᴏᴀᴅᴄᴀꜱᴛ&#4ef89b] &f→&r");
        toggleOnMessage = configuration.getString("messages.toggle-on", "{prefix} &#4ef89b✔ &fɴᴏᴛɪꜰɪᴄᴀᴛɪᴏɴꜱ ʜᴀᴠᴇ ʙᴇᴇɴ &#4ef89bᴇɴᴀʙʟᴇᴅ!");
        toggleOffMessage = configuration.getString("messages.toggle-off", "{prefix} &#FF4141✘ &fɴᴏᴛɪꜰɪᴄᴀᴛɪᴏɴꜱ ʜᴀᴠᴇ ʙᴇᴇɴ &#FF4141ᴅɪꜱᴀʙʟᴇᴅ!");
        noPermissionMessage = configuration.getString("messages.no-permission", "{prefix} &#FF4141✘ ʏᴏᴜ ᴅᴏɴ'ᴛ ʜᴀᴠᴇ ᴘᴇʀᴍɪꜱꜱɪᴏɴ ᴛᴏ ᴜꜱᴇ ᴛʜɪꜱ ᴄᴏᴍᴍᴀɴᴅ!");
        reloadMessage = configuration.getString("messages.reload", "{prefix} &#4ef89b✔ &fᴄᴏɴꜰɪɢᴜʀᴀᴛɪᴏɴ ʀᴇʟᴏᴀᴅᴇᴅ ɪɴ &#4ef89b%ms%ᴍꜱ!");
        playerOnlyMessage = configuration.getString("messages.player-only", "{prefix} &#FF4141✘ ᴛʜɪꜱ ᴄᴏᴍᴍᴀɴᴅ ᴄᴀɴ ᴏɴʟʏ ʙᴇ ᴜꜱᴇᴅ ʙʏ ᴘʟᴀʏᴇʀꜱ!");
        announceSentMessage = configuration.getString("messages.announce-sent", "{prefix} &#4EF89B✔ ᴀɴɴᴏᴜɴᴄᴇᴍᴇɴᴛ ꜱᴇɴᴛ!");
        announceNotFoundMessage = configuration.getString("messages.announce-not-found", "{prefix} &#FF4141✘ &fBroadcast with id '&#FF4141{id}&f' not found.");
        helpMessage = configuration.getStringList("messages.help");
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

    public String getPrefix() {
        return prefix;
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

    public List<String> getHelpMessage() {
        return helpMessage;
    }

    public String getAnnounceNotFoundMessage() {
        return announceNotFoundMessage;
    }
}
