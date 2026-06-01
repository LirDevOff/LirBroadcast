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
import itz.lirdev.tools.SchedulerUtil;

public class LirBroadcast extends JavaPlugin {

    private Config config;
    private ActionHandler actionHandler;
    private DatabaseManager databaseManager;
    private BroadcastManager broadcastManager;

    private static LirBroadcast INSTANCE;
    private long startTime;

    public static LirBroadcast getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        startTime = System.currentTimeMillis();

        Logger.init(config, getName());

        config = new Config(this);
        ColorParser.setConfig(config);

        boolean hasPapi = getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
        ColorParser.setPapiEnabled(hasPapi);
        if (!hasPapi) {
            Logger.warn("PlaceholderAPI not found. Placeholders will not work.");
        }

        actionHandler = new ActionHandler(this);
        databaseManager = new DatabaseManager(getDataFolder());
        broadcastManager = new BroadcastManager(this, actionHandler, databaseManager, config);

        broadcastManager.loadBroadcasts();
        broadcastManager.startBroadcasting();

        SchedulerUtil.runAsync(this, () -> new Metrics(this, 30693));

        CommandsHandler commandsHandler = new CommandsHandler(config, broadcastManager, databaseManager);

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
            UpdateChecker updateChecker = new UpdateChecker(this, getDescription().getVersion(), "lirbroadcast", config);
            updateChecker.start();
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

    private void printStartupMessage() {
        Logger.msg("");
        Logger.msg("👋 Plugin &#4EF89BLirBroadcast&f loaded successfully");
        Logger.msg("📊 Information");
        Logger.msg("  ├ Version: &#4EF89B" + getDescription().getVersion());
        Logger.msg("  ├ Authors: &#4EF89B" + String.join(", ", getDescription().getAuthors()));
        Logger.msg("  ├ Core: &#4EF89B" + detectServerSoftware());
        Logger.msg("  ├ Link: &#4EF89Bhttps://modrinth.com/project/lirbroadcast");
        Logger.msg("✅ &#4EF89BThank you for using LirBroadcast!");
        Logger.msg("");
    }

    private void printShutdownMessage() {
        long uptime = System.currentTimeMillis() - startTime;
        long hours = uptime / 3_600_000;
        long minutes = (uptime % 3_600_000) / 60_000;
        long seconds = (uptime % 60_000) / 1_000;

        StringBuilder uptimeStr = new StringBuilder();
        if (hours > 0) {
            uptimeStr.append(hours).append("h ");
        }
        if (minutes > 0 || hours > 0) {
            uptimeStr.append(minutes).append("m ");
        }
        uptimeStr.append(seconds).append("s");

        Logger.msg("");
        Logger.msg("👋 Plugin &#4EF89BLirBroadcast&f is disabling...");
        Logger.msg("📊 Session statistics:");
        Logger.msg("  ├ Version: &#4EF89B" + getDescription().getVersion());
        Logger.msg("  ├ Uptime:  &#4EF89B" + uptimeStr);
        Logger.msg("  ├ Core: &#4EF89B" + detectServerSoftware());
        Logger.msg("✅ &#4EF89BThank you for using LirBroadcast!");
        Logger.msg("");
    }

    private boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private String detectServerSoftware() {
        if (classExists("io.papermc.paper.threadedregions.RegionizedServer")) {
            return "Folia";
        }
        if (classExists("io.papermc.paper.PaperConfig")) {
            return "Paper";
        }
        if (classExists("org.purpurmc.purpur.PurpurConfig")) {
            return "Purpur";
        }
        if (classExists("gg.pufferfish.pufferfish.PufferfishConfig")) {
            return "Pufferfish";
        }
        if (classExists("net.md_5.bungee.api.ChatColor")) {
            return "Spigot";
        }
        return "Bukkit";
    }
}
