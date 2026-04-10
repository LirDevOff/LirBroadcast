package itz.lirdev;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import itz.lirdev.actions.ActionHandler;
import itz.lirdev.commands.CommandsHandler;
import itz.lirdev.configuration.Config;
import itz.lirdev.managers.BroadcastManager;
import itz.lirdev.managers.DatabaseManager;
import itz.lirdev.tools.ColorParser;
import itz.lirdev.tools.Logger;

public class LirBroadcast extends JavaPlugin {

    private Config config;
    private ActionHandler actionHandler;
    private DatabaseManager databaseManager;
    private BroadcastManager broadcastManager;

    private static LirBroadcast INSTANCE;

    public static LirBroadcast getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        config = new Config(this);

        ColorParser.setConfig(config);

        int pluginId = 30693;
        Metrics metrics = new Metrics(this, pluginId);

        setupPlaceholders();

        Logger.init(config, getName());

        actionHandler = new ActionHandler(this);
        databaseManager = new DatabaseManager(getDataFolder());
        broadcastManager = new BroadcastManager(this, actionHandler, databaseManager, config);

        broadcastManager.loadBroadcasts();
        broadcastManager.startBroadcasting();

        CommandsHandler commandsHandler = new CommandsHandler(config, broadcastManager);

        var notificationCmd = getCommand("notification");
        var lirbroadcastCmd = getCommand("lirbroadcast");

        if (notificationCmd != null) {
            notificationCmd.setExecutor(commandsHandler);
            notificationCmd.setTabCompleter(commandsHandler);
        }

        if (lirbroadcastCmd != null) {
            lirbroadcastCmd.setExecutor(commandsHandler);
            lirbroadcastCmd.setTabCompleter(commandsHandler);
            lirbroadcastCmd.setPermission("lirbroadcast.admin");
        }

        if (config.isCheckUpdate()) {
            new UpdateChecker(this, getDescription().getVersion(), "lirbroadcast", config);
        }

        printStartupMessage();
    }

    @Override
    public void onDisable() {
        if (broadcastManager != null) {
            broadcastManager.stopBroadcasting();
        }
        if (databaseManager != null) {
            databaseManager.close();
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
