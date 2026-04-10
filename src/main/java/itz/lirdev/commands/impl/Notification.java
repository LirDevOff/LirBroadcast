package itz.lirdev.commands.impl;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import itz.lirdev.configuration.Config;
import itz.lirdev.managers.BroadcastManager;
import itz.lirdev.tools.ColorParser;

public class Notification {

    private final Config config;
    private final BroadcastManager broadcastManager;

    public Notification(Config config, BroadcastManager broadcastManager) {
        this.config = config;
        this.broadcastManager = broadcastManager;
    }

    public boolean execute(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorParser.colorize(config.getPlayerOnlyMessage()));
            return true;
        }

        Player player = (Player) sender;
        boolean isCurrentlyDisabled = broadcastManager.isNotificationDisabled(player.getUniqueId());
        boolean shouldEnable = isCurrentlyDisabled;
        broadcastManager.toggleNotifications(player.getUniqueId(), shouldEnable);

        String message = shouldEnable ? config.getToggleOnMessage() : config.getToggleOffMessage();
        sender.sendMessage(ColorParser.colorize(message));

        return true;
    }

}
