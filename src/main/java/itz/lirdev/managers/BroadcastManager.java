package itz.lirdev.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import itz.lirdev.actions.Action;
import itz.lirdev.actions.ActionHandler;
import itz.lirdev.actions.impl.WaitAction;
import itz.lirdev.configuration.Config;
import itz.lirdev.tools.Logger;
import itz.lirdev.tools.SchedulerUtil;

public class BroadcastManager {

    private final JavaPlugin plugin;
    private final ActionHandler actionHandler;
    private final DatabaseManager databaseManager;
    private final Config config;

    private final List<BroadcastMessage> broadcastMessages = new ArrayList<>();
    private final Set<UUID> disabledCache = ConcurrentHashMap.newKeySet();
    private final List<Player> targetBuffer = new ArrayList<>(64);

    private Object broadcastTask = null;
    private int currentIndex = 0;

    private static final long MIN_INTERVAL_TICKS = 1L;
    private static final int SEND_BATCH_SIZE = 50;

    public BroadcastManager(JavaPlugin plugin,
            ActionHandler actionHandler,
            DatabaseManager databaseManager,
            Config config) {
        this.plugin = plugin;
        this.actionHandler = actionHandler;
        this.databaseManager = databaseManager;
        this.config = config;
    }

    public void loadBroadcasts() {
        broadcastMessages.clear();
        disabledCache.clear();
        currentIndex = 0;

        disabledCache.addAll(databaseManager.getDisabledSet());

        ConfigurationSection section = config.getConfig()
                .getConfigurationSection("messages.notifications");

        if (section == null) {
            Logger.warn("No broadcasts found in config");
            return;
        }

        for (String key : section.getKeys(false)) {
            List<String> lines = config.getConfig()
                    .getStringList("messages.notifications." + key);
            List<Action> actions = actionHandler.parseActions(lines);
            if (!actions.isEmpty()) {
                broadcastMessages.add(new BroadcastMessage(key, actions));
            }
        }

        Logger.debug("Loaded " + broadcastMessages.size() + " broadcasts");
    }

    public void startBroadcasting() {
        stopBroadcasting();
        long interval = Math.max(MIN_INTERVAL_TICKS, config.getInterval() * 20L);
        broadcastTask = SchedulerUtil.runTaskTimer(plugin, this::broadcast, interval, interval);
    }

    public void stopBroadcasting() {
        if (broadcastTask != null) {
            SchedulerUtil.cancelTask(broadcastTask);
            broadcastTask = null;
        }
    }

    private void broadcast() {
        if (broadcastMessages.isEmpty()) {
            return;
        }

        int onlineCount = Bukkit.getOnlinePlayers().size();
        if (onlineCount == 0) {
            return;
        }

        send(selectMessage(), onlineCount);
    }

    private BroadcastMessage selectMessage() {
        if (config.isRandom()) {
            return broadcastMessages.get(ThreadLocalRandom.current().nextInt(broadcastMessages.size()));
        }
        if (currentIndex >= broadcastMessages.size()) {
            currentIndex = 0;
        }
        return broadcastMessages.get(currentIndex++);
    }

    private void send(BroadcastMessage message, int expectedSize) {
        Collection<? extends Player> online = Bukkit.getOnlinePlayers();
        if (online.isEmpty()) {
            return;
        }

        List<Action> actions = message.getActions();

        if (message.hasWait()) {
            for (Player player : online) {
                if (disabledCache.contains(player.getUniqueId())) {
                    continue;
                }
                actionHandler.executeActions(player, actions);
            }
            return;
        }

        List<Player> targets = targetBuffer;
        targets.clear();

        boolean hasDisabled = !disabledCache.isEmpty();
        for (Player player : online) {
            if (hasDisabled && disabledCache.contains(player.getUniqueId())) {
                continue;
            }
            if (player.isOnline()) {
                targets.add(player);
            }
        }

        int size = targets.size();
        if (size == 0) {
            return;
        }

        if (size <= SEND_BATCH_SIZE) {
            sendToList(targets, actions, size);
        } else {
            for (int i = 0; i < size; i += SEND_BATCH_SIZE) {
                final int from = i;
                final int to = Math.min(i + SEND_BATCH_SIZE, size);
                final List<Player> batch = new ArrayList<>(targets.subList(from, to));
                final long tickDelay = (long) (i / SEND_BATCH_SIZE);
                if (tickDelay == 0) {
                    sendToList(batch, actions, batch.size());
                } else {
                    SchedulerUtil.runTaskLater(plugin, () -> sendToList(batch, actions, batch.size()), tickDelay);
                }
            }
        }
    }

    private void sendToList(List<Player> players, List<Action> actions, int playerCount) {
        int actionCount = actions.size();
        for (int p = 0; p < playerCount; p++) {
            Player player = players.get(p);
            if (!player.isOnline()) {
                continue;
            }
            for (int a = 0; a < actionCount; a++) {
                actions.get(a).execute(player);
            }
        }
    }

    public boolean broadcastMessageById(String id) {
        for (BroadcastMessage msg : broadcastMessages) {
            if (msg.getName().equalsIgnoreCase(id)) {
                send(msg, Bukkit.getOnlinePlayers().size());
                return true;
            }
        }
        return false;
    }

    public void broadcastRandom() {
        if (broadcastMessages.isEmpty()) {
            return;
        }
        send(broadcastMessages.get(
                ThreadLocalRandom.current().nextInt(broadcastMessages.size())),
                Bukkit.getOnlinePlayers().size());
    }

    public void toggle(UUID uuid, boolean enable) {
        if (enable) {
            databaseManager.enableNotifications(uuid);
            disabledCache.remove(uuid);
        } else {
            databaseManager.disableNotifications(uuid);
            disabledCache.add(uuid);
        }
    }

    public List<BroadcastMessage> getMessages() {
        return Collections.unmodifiableList(broadcastMessages);
    }

    public List<String> getBroadcastMessageIds() {
        List<String> ids = new ArrayList<>(broadcastMessages.size());
        for (BroadcastMessage msg : broadcastMessages) {
            ids.add(msg.getName());
        }
        return ids;
    }

    public static class BroadcastMessage {

        private final String name;
        private final List<Action> actions;
        private final boolean hasWait;

        public BroadcastMessage(String name, List<Action> actions) {
            this.name = name;
            this.actions = List.copyOf(actions);
            boolean wait = false;
            for (Action a : actions) {
                if (a instanceof WaitAction) {
                    wait = true;
                    break;
                }
            }
            this.hasWait = wait;
        }

        public String getName() {
            return name;
        }

        public List<Action> getActions() {
            return actions;
        }

        public boolean hasWait() {
            return hasWait;
        }
    }
}
