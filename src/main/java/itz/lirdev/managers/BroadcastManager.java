package itz.lirdev.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import itz.lirdev.actions.Action;
import itz.lirdev.actions.ActionHandler;
import itz.lirdev.configuration.Config;
import itz.lirdev.tools.Logger;

public class BroadcastManager {

    private final JavaPlugin plugin;
    private final ActionHandler actionHandler;
    private final DatabaseManager databaseManager;
    private final Config config;

    private List<BroadcastMessage> broadcastMessages;
    private int taskId = -1;
    private int currentMessageIndex = 0;

    public BroadcastManager(JavaPlugin plugin, ActionHandler actionHandler, DatabaseManager databaseManager, Config config) {
        this.plugin = plugin;
        this.actionHandler = actionHandler;
        this.databaseManager = databaseManager;
        this.config = config;
        this.broadcastMessages = new ArrayList<>();
    }

    public void loadBroadcasts() {
        broadcastMessages.clear();
        currentMessageIndex = 0;
        databaseManager.loadAllDisabledPlayers();

        var notificationsSection = config.getConfig().getConfigurationSection("messages.notifications");
        if (notificationsSection == null) {
            Logger.warn("No broadcasts found in configuration");
            return;
        }

        for (String key : notificationsSection.getKeys(false)) {
            List<String> actionLines = (List<String>) notificationsSection.get(key);
            if (actionLines != null) {
                List<Action> actions = actionHandler.parseActions(actionLines);
                broadcastMessages.add(new BroadcastMessage(key, actions));
            }
        }

        Logger.info("Loaded " + broadcastMessages.size() + " broadcasts");
    }

    public void startBroadcasting() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }

        long interval = Math.max(1, config.getInterval() * 20L);

        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::broadcast, interval, interval);
    }

    public void stopBroadcasting() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    private void broadcast() {
        if (broadcastMessages.isEmpty()) {
            return;
        }

        BroadcastMessage message = config.isRandom() ? getRandomMessage() : broadcastMessages.get(currentMessageIndex);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!databaseManager.isNotificationDisabled(player.getUniqueId())) {
                actionHandler.executeActions(player, message.getActions());
            }
        }

        if (!config.isRandom()) {
            currentMessageIndex = (currentMessageIndex + 1) % broadcastMessages.size();
        }
    }

    private BroadcastMessage getRandomMessage() {
        Random random = new Random();
        return broadcastMessages.get(random.nextInt(broadcastMessages.size()));
    }

    public void reloadBroadcasts() {
        stopBroadcasting();
        loadBroadcasts();
        startBroadcasting();
    }

    public boolean toggleNotifications(java.util.UUID playerUUID, boolean enable) {
        if (enable) {
            databaseManager.enableNotifications(playerUUID);
        } else {
            databaseManager.disableNotifications(playerUUID);
        }
        return enable;
    }

    public boolean isNotificationDisabled(java.util.UUID playerUUID) {
        return databaseManager.isNotificationDisabled(playerUUID);
    }

    public void broadcastReloadMessage(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!databaseManager.isNotificationDisabled(player.getUniqueId())) {
                player.sendMessage(message);
            }
        }
    }

    public void broadcastTestMessage() {
        if (broadcastMessages.isEmpty()) {
            Logger.warn("Cannot send test message: no broadcasts loaded");
            return;
        }

        BroadcastMessage message = getRandomMessage();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!databaseManager.isNotificationDisabled(player.getUniqueId())) {
                actionHandler.executeActions(player, message.getActions());
            }
        }
    }

    public List<String> getBroadcastMessageIds() {
        List<String> ids = new ArrayList<>();
        for (BroadcastMessage message : broadcastMessages) {
            ids.add(message.getName());
        }
        return ids;
    }

    public void broadcastMessageById(String messageId) {
        for (BroadcastMessage message : broadcastMessages) {
            if (message.getName().equalsIgnoreCase(messageId)) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!databaseManager.isNotificationDisabled(player.getUniqueId())) {
                        actionHandler.executeActions(player, message.getActions());
                    }
                }
                return;
            }
        }
    }

    private static class BroadcastMessage {

        private final String name;
        private final List<Action> actions;

        public BroadcastMessage(String name, List<Action> actions) {
            this.name = name;
            this.actions = actions;
        }

        public String getName() {
            return name;
        }

        public List<Action> getActions() {
            return new ArrayList<>(actions);
        }
    }

}
