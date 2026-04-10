package itz.lirdev.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import itz.lirdev.commands.impl.Notification;
import itz.lirdev.commands.impl.admin.Announce;
import itz.lirdev.commands.impl.admin.Reload;
import itz.lirdev.configuration.Config;
import itz.lirdev.managers.BroadcastManager;
import itz.lirdev.tools.ColorParser;

public class CommandsHandler implements CommandExecutor, TabCompleter {

    private final Reload reloadCommand;
    private final Announce announceCommand;
    private final Notification notificationCommand;
    private final Config config;
    private final BroadcastManager broadcastManager;

    public CommandsHandler(Config config, BroadcastManager broadcastManager) {
        this.config = config;
        this.broadcastManager = broadcastManager;
        this.reloadCommand = new Reload(config, broadcastManager);
        this.announceCommand = new Announce(config, broadcastManager);
        this.notificationCommand = new Notification(config, broadcastManager);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (command.getName().equalsIgnoreCase("notification")) {
            return notificationCommand.execute(sender);
        }

        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("lirbroadcast.admin")) {
                sender.sendMessage(ColorParser.colorize(config.getNoPermissionMessage()));
                return true;
            }
            reloadCommand.reloadConfig(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("announce")) {
            if (args.length < 2) {
                sendHelp(sender, label);
            } else {
                announceCommand.announce(sender, args);
            }
            return true;
        }

        sendHelp(sender, label);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("notification")) {
            return completions;
        }

        if (args.length == 1) {
            if (sender.hasPermission("lirbroadcast.admin")) {
                completions.add("reload");
                completions.add("announce");
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("announce")) {
            if (sender.hasPermission("lirbroadcast.admin")) {
                completions.addAll(broadcastManager.getBroadcastMessageIds());
            }
        }

        return completions;
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage(ColorParser.colorize(""));
        sender.sendMessage(ColorParser.colorize(" &#4EF89B[&lUsage&#4EF89B]"));
        sender.sendMessage(ColorParser.colorize(""));
        sender.sendMessage(ColorParser.colorize(" &#4EF89B• /" + label + " reload &7(reload configuration)"));
        sender.sendMessage(ColorParser.colorize(" &#4EF89B• /" + label + " announce {id} &7(manual broadcast)"));
        sender.sendMessage(ColorParser.colorize(""));
    }

}
