package lirdev.lirbroadcast;

import lirdev.lirbroadcast.managers.ConfigManager;
import lirdev.lirbroadcast.managers.NotificationManager;
import lirdev.lirbroadcast.utils.ColorParser;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class LirBroadcast extends JavaPlugin {
    private ConfigManager configManager;
    private NotificationManager notificationManager;
    private final Logger log = Logger.getLogger("Minecraft");

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.notificationManager = new NotificationManager(this, configManager);

        Commands command = new Commands(notificationManager, configManager);
        getCommand("notification").setExecutor(command);

        getCommand("lirbroadcast").setExecutor(command);
        getCommand("lirbroadcast").setTabCompleter(command);

        if (configManager.isCheckupdate()) {
            new UpdateChecker(this, getDescription().getVersion(), "lirbroadcast", configManager);
        }

        printStartupMessage();
    }

    @Override
    public void onDisable() {
        if (notificationManager != null) {
            notificationManager.disable();
        }
        printShutdownMessage();
    }

    private void printStartupMessage() {
        log.info(ColorParser.colorize(""));
        log.info(ColorParser.colorize("👋 Plugin &#4EF89BLirBroadcast&f loaded successfully"));
        log.info(ColorParser.colorize("📊 Information"));
        log.info(ColorParser.colorize("  ├ Version: &#4EF89B" + this.getDescription().getVersion()));
        log.info(ColorParser.colorize("  ├ Authors: &#4EF89B" + String.join(", ", this.getDescription().getAuthors())));
        log.info(ColorParser.colorize("  ├ Link: &#4EF89Bhttps://modrinth.com/project/lirbroadcast"));
        log.info(ColorParser.colorize("✅ &#4EF89BThank you for using LirBroadcast!"));
        log.info(ColorParser.colorize(""));
    }

    private void printShutdownMessage() {
        log.info(ColorParser.colorize(""));
        log.info(ColorParser.colorize("👋 Plugin &#4EF89BLirBroadcast&f is disabling..."));
        log.info(ColorParser.colorize("📊 Session statistics:"));
        log.info(ColorParser.colorize("  ├ Version: &#4EF89B" + this.getDescription().getVersion()));
        log.info(ColorParser.colorize("  ├ Uptime: &#4EF89Bcompleted"));
        log.info(ColorParser.colorize("✅ &#4EF89BThank you for using LirBroadcast!"));
        log.info(ColorParser.colorize(""));
    }
}