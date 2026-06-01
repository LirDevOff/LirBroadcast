package itz.lirdev.commands.impl;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import itz.lirdev.configuration.Config;
import itz.lirdev.managers.BroadcastManager;
import itz.lirdev.managers.DatabaseManager;
import itz.lirdev.tools.ColorParser;

public class Notification {

    private final Config config;
    private final BroadcastManager broadcastManager;
    private final DatabaseManager databaseManager;

    public Notification(Config config, BroadcastManager broadcastManager, DatabaseManager databaseManager) {
        this.config = config;
        this.broadcastManager = broadcastManager;
        this.databaseManager = databaseManager;
    }

    public boolean execute(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorParser.colorize(config.getPlayerOnlyMessage()));
            return true;
        }

        Player player = (Player) sender;
        boolean isCurrentlyDisabled = databaseManager.isNotificationDisabled(player.getUniqueId());
        boolean shouldEnable = isCurrentlyDisabled;
        broadcastManager.toggle(player.getUniqueId(), shouldEnable);

        String message = shouldEnable ? config.getToggleOnMessage() : config.getToggleOffMessage();
        sender.sendMessage(ColorParser.colorize(message));

        return true;
    }
}
