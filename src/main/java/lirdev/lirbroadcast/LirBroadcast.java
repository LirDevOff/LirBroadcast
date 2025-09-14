package lirdev.lirbroadcast;

import lirdev.lirbroadcast.configuration.Config;
import lirdev.lirbroadcast.managers.NotificationManager;
import lirdev.lirbroadcast.utils.ColorParser;
import lirdev.lirbroadcast.utils.Logger;
import lombok.Getter;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class LirBroadcast extends JavaPlugin {
    @Getter
    private Config cfg;
    private NotificationManager notificationManager;

    private static LirBroadcast INSTANCE;

    public static LirBroadcast getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        cfg = new Config(this);
        notificationManager = new NotificationManager(this, cfg);

        setupPlaceholders();

        Logger.init(cfg, this.getName());

        Commands notification = new Commands(notificationManager, cfg);
        PluginCommand notificationCmd = getCommand("notification");
        PluginCommand lirbroadcastCmd = getCommand("lirbroadcast");
        if (notificationCmd!=null)
            notificationCmd.setExecutor(notification);
        if (lirbroadcastCmd!=null)
            lirbroadcastCmd.setExecutor(notification);

        if (cfg.isCheckupdate()) {
            new UpdateChecker(this, getDescription().getVersion(), "lirbroadcast", cfg);
        }

        printStartupMessage();
    }

    @Override
    public void onDisable() {
        if (notificationManager != null) {
            notificationManager.disable(false);
        }
        printShutdownMessage();
    }

    private void setupPlaceholders() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            Logger.warn("PlaceholderAPI not found. Placeholders are not being working");
        }
    }
    private void printStartupMessage() {
        Logger.msg("");
        Logger.msg("👋 Plugin &#4EF89BLirBroadcast&f loaded successfully");
        Logger.msg("📊 Information");
        Logger.msg("  ├ Version: &#4EF89B" + getDescription().getVersion());
        Logger.msg("  ├ Authors: &#4EF89B" + String.join(", ", getDescription().getAuthors()));
        Logger.msg("  ├ Link: &#4EF89Bhttps://modrinth.com/project/lirbroadcast");
        Logger.msg("✅ &#4EF89BThank you for using LirBroadcast!");
        Logger.msg("");
    }

    private void printShutdownMessage() {
        Logger.msg("");
        Logger.msg("👋 Plugin &#4EF89BLirBroadcast&f is disabling...");
        Logger.msg("📊 Session statistics:");
        Logger.msg("  ├ Version: &#4EF89B" + getDescription().getVersion());
        Logger.msg("  ├ Uptime: &#4EF89Bcompleted");
        Logger.msg("✅ &#4EF89BThank you for using LirBroadcast!");
        Logger.msg("");
    }
}