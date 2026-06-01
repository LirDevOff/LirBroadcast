package itz.lirdev.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import itz.lirdev.LirBroadcast;
import itz.lirdev.commands.impl.Notification;
import itz.lirdev.commands.impl.admin.Announce;
import itz.lirdev.commands.impl.admin.Reload;
import itz.lirdev.configuration.Config;
import itz.lirdev.managers.BroadcastManager;
import itz.lirdev.managers.DatabaseManager;
import itz.lirdev.tools.ColorParser;

public class CommandsHandler implements CommandExecutor, TabCompleter {

    private final Reload reloadCommand;
    private final Announce announceCommand;
    private final Notification notificationCommand;
    private final Config config;
    private final BroadcastManager broadcastManager;

    public CommandsHandler(Config config, BroadcastManager broadcastManager, DatabaseManager databaseManager) {
        this.config = config;
        this.broadcastManager = broadcastManager;
        this.reloadCommand = new Reload(LirBroadcast.getInstance(), config, broadcastManager);
        this.announceCommand = new Announce(config, broadcastManager);
        this.notificationCommand = new Notification(config, broadcastManager, databaseManager);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (command.getName().equalsIgnoreCase("notification")) {
            return notificationCommand.execute(sender);
        }

        if (args.length == 0) {
            helpMassage(sender, label);
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
                helpMassage(sender, label);
            } else {
                announceCommand.announce(sender, args);
            }
            return true;
        }

        helpMassage(sender, label);
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
                List<String> subCommands = List.of("reload", "announce");

                String input = args[0].toLowerCase();
                for (String cmd : subCommands) {
                    if (cmd.startsWith(input)) {
                        completions.add(cmd);
                    }
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("announce")) {
            if (sender.hasPermission("lirbroadcast.admin")) {
                String input = args[1].toLowerCase();
                for (String id : broadcastManager.getBroadcastMessageIds()) {
                    if (id.toLowerCase().startsWith(input)) {
                        completions.add(id);
                    }
                }
            }
        }

        return completions;
    }

    private void helpMassage(CommandSender sender, String label) {
        List<String> lines = config.getHelpMessage();

        if (lines == null || lines.isEmpty()) {
            sender.sendMessage(ColorParser.colorize("/" + label + " reload"));
            sender.sendMessage(ColorParser.colorize("/" + label + " announce {id}"));
            return;
        }

        for (String line : lines) {
            sender.sendMessage(ColorParser.colorize(line.replace("{cmd}", label)));
        }
    }
}
