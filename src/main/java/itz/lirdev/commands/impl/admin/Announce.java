package itz.lirdev.commands.impl.admin;

import org.bukkit.command.CommandSender;

import itz.lirdev.configuration.Config;
import itz.lirdev.managers.BroadcastManager;
import itz.lirdev.tools.ColorParser;

public class Announce {

    private final Config config;
    private final BroadcastManager broadcastManager;

    public Announce(Config config, BroadcastManager broadcastManager) {
        this.config = config;
        this.broadcastManager = broadcastManager;
    }

    public void announce(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lirbroadcast.admin")) {
            sender.sendMessage(ColorParser.colorize(config.getNoPermissionMessage()));
            return;
        }

        if (args.length < 2) {
            return;
        }

        String messageId = args[1];
        broadcastManager.broadcastMessageById(messageId);
        sender.sendMessage(ColorParser.colorize(config.getAnnounceSentMessage()));
    }
}
