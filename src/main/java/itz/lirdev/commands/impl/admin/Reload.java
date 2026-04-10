package itz.lirdev.commands.impl.admin;

import org.bukkit.command.CommandSender;

import itz.lirdev.configuration.Config;
import itz.lirdev.managers.BroadcastManager;
import itz.lirdev.tools.ColorParser;

public class Reload {

    private final Config config;
    private final BroadcastManager broadcastManager;

    public Reload(Config config, BroadcastManager broadcastManager) {
        this.config = config;
        this.broadcastManager = broadcastManager;
    }

    public void reloadConfig(CommandSender sender) {
        if (!sender.hasPermission("lirbroadcast.admin")) {
            sender.sendMessage(ColorParser.colorize(config.getNoPermissionMessage()));
            return;
        }

        long startTime = System.currentTimeMillis();

        config.load();
        broadcastManager.reloadBroadcasts();

        long endTime = System.currentTimeMillis();
        long elapsedMs = endTime - startTime;

        String message = config.getReloadMessage().replace("%ms%", String.valueOf(elapsedMs));
        sender.sendMessage(ColorParser.colorize(message));

        broadcastManager.broadcastTestMessage();
    }

}
