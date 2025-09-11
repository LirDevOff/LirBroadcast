package lirdev.lirbroadcast;

import lirdev.lirbroadcast.managers.ConfigManager;
import lirdev.lirbroadcast.managers.NotificationManager;
import lirdev.lirbroadcast.utils.ColorParser;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Commands implements CommandExecutor, TabCompleter {
    private final NotificationManager notificationManager;
    private final ConfigManager configManager;

    public Commands(NotificationManager notificationManager, ConfigManager configManager) {
        this.notificationManager = notificationManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("lirbroadcast") && !sender.hasPermission("lirbroadcast.admin")) {
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("lirbroadcast")) {
            return handleAdminCommand(sender, args);
        }

        return handlePlayerCommand(sender);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (cmd.getName().equalsIgnoreCase("lirbroadcast") && !sender.hasPermission("lirbroadcast.admin")) {
            return new ArrayList<>();
        }

        if (cmd.getName().equalsIgnoreCase("lirbroadcast")) {
            List<String> completions = new ArrayList<>();
            if (args.length == 1) {
                String input = args[0].toLowerCase();
                if ("reload".startsWith(input)) {
                    completions.add("reload");
                }
            }
            return completions;
        }

        return null;
    }

    private boolean handleAdminCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                configManager.reloadConfig();
                notificationManager.reload();
                sender.sendMessage(ColorParser.fullFormat(
                        configManager.getReloadMsg(),
                        null
                ));
            } else {
                sendHelpMessage(sender);
            }
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            configManager.reloadConfig();
            notificationManager.reload();
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 1.0f, 1.0f);
            sender.sendMessage(ColorParser.fullFormat(
                    configManager.getReloadMsg(),
                    player
            ));
            return true;
        }

        sendHelpMessage(sender);
        return true;
    }

    private boolean handlePlayerCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorParser.fullFormat(
                    configManager.getPlayerOnlyMsg(),
                    null
            ));
            return true;
        }
        notificationManager.toggleNotifications((Player) sender);
        return true;
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ColorParser.colorize(""));
        sender.sendMessage(ColorParser.colorize(" &#4EF89B[&lUsage&#4EF89B]  "));
        sender.sendMessage(ColorParser.colorize(""));
        sender.sendMessage(ColorParser.colorize(" &#4EF89B• /lirbroadcast reload &7(reloads the plugin configuration)"));
        sender.sendMessage(ColorParser.colorize(""));
    }
}