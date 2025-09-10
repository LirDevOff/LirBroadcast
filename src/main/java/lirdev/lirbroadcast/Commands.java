package lirdev.lirbroadcast;

import lirdev.lirbroadcast.managers.ConfigManager;
import lirdev.lirbroadcast.managers.NotificationManager;
import lirdev.lirbroadcast.utils.ColorParser;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {
    private final NotificationManager notificationManager;
    private final ConfigManager configManager;

    public Commands(NotificationManager notificationManager, ConfigManager configManager) {
        this.notificationManager = notificationManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("lirbroadcast")) {
            return handleAdminCommand(sender, args);
        }

        return handlePlayerCommand(sender);
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
            }
            return true;
        }

        Player player = (Player) sender;

        if (!sender.hasPermission("lirbroadcast.admin")) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 1.0f, 1.0f);
            sender.sendMessage(ColorParser.fullFormat(
                    configManager.getNoPermissionMsg(),
                    player
            ));
            return true;
        }

        if (args.length == 0) {
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
}