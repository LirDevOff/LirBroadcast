package itz.lirdev.commands.impl.admin;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import itz.lirdev.configuration.Config;
import itz.lirdev.managers.BroadcastManager;
import itz.lirdev.tools.ColorParser;

public class Reload {

    private final JavaPlugin plugin;
    private final Config config;
    private final BroadcastManager broadcastManager;

    public Reload(JavaPlugin plugin, Config config, BroadcastManager broadcastManager) {
        this.plugin = plugin;
        this.config = config;
        this.broadcastManager = broadcastManager;
    }

    public void reloadConfig(CommandSender sender) {
        long start = System.nanoTime();
        config.load();

        broadcastManager.loadBroadcasts();
        broadcastManager.startBroadcasting();

        String elapsed = String.format("%.2f", (System.nanoTime() - start) / 1_000_000.0);
        String message = config.getReloadMessage().replace("%ms%", elapsed);

        sender.sendMessage(ColorParser.colorize(message));
    }
}
