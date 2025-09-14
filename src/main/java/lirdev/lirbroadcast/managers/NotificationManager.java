package lirdev.lirbroadcast.managers;

import lirdev.lirbroadcast.LirBroadcast;
import lirdev.lirbroadcast.storage.JSON;
import lirdev.lirbroadcast.utils.ColorParser;
import lirdev.lirbroadcast.utils.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationManager {
    private final LirBroadcast plugin;
    private final ConfigManager configManager;
    private final JSON jsonData;

    private final Set<UUID> disabledPlayers = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private volatile BukkitTask notificationTask;
    private final Random random = new Random();
    private final List<String[]> formattedMessageCache = new ArrayList<>();
    private int currentMessageIndex = 0;

    public NotificationManager(LirBroadcast plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.jsonData = new JSON(plugin, "disabled_players.json");
        initialize();
    }

    private void initialize() {
        plugin.getDataFolder().mkdirs();
        reloadMessageCache();
        loadDisabledPlayers();
        startNotificationTask();
        startAutoSaveTask();
    }

    private void loadDisabledPlayers() {
        Set<String> disabledUuids = jsonData.getSet("disabled_players", String.class, new HashSet<>());
        disabledPlayers.clear();
        for (String uuidStr : disabledUuids) {
            try {
                disabledPlayers.add(UUID.fromString(uuidStr));
            } catch (IllegalArgumentException e) {}
        }
    }

    private void saveDisabledPlayers() {
        Set<String> uuidStrings = new HashSet<>();
        for (UUID uuid : disabledPlayers) {
            uuidStrings.add(uuid.toString());
        }
        jsonData.setAndSaveAsync("disabled_players", uuidStrings);
    }

    private void startAutoSaveTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::saveDisabledPlayers, 6000L, 6000L);
    }

    private void startNotificationTask() {
        if (notificationTask != null) {
            notificationTask.cancel();
        }

        long intervalTicks = configManager.getInterval() * 20L;
        notificationTask = new BukkitRunnable() {
            @Override
            public void run() {
                sendBroadcastMessage();
            }
        }.runTaskTimer(plugin, 20L, intervalTicks);
    }

    private void sendBroadcastMessage() {
        if (formattedMessageCache.isEmpty()) {
            Logger.warn("No messages available for broadcast");
            return;
        }

        String[] messageLines = getNextMessage();
        if (messageLines.length == 0) {
            return;
        }

        boolean soundEnabled = configManager.isSoundEnabled();
        Sound sound = configManager.getSound();
        float volume = configManager.getVolume();
        float pitch = configManager.getPitch();

        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();

        for (Player player : onlinePlayers) {
            if (disabledPlayers.contains(player.getUniqueId())) continue;
            player.sendMessage(messageLines);
            if (soundEnabled) {
                player.playSound(player.getLocation(), sound, volume, pitch);
            }
        }
    }

    private synchronized String[] getNextMessage() {
        if (formattedMessageCache.isEmpty()) {
            return new String[0];
        }

        if (configManager.isRandom()) {
            return formattedMessageCache.get(random.nextInt(formattedMessageCache.size()));
        } else {
            String[] message = formattedMessageCache.get(currentMessageIndex);
            currentMessageIndex = (currentMessageIndex + 1) % formattedMessageCache.size();
            return message;
        }
    }

    private synchronized void reloadMessageCache() {
        try {
            formattedMessageCache.clear();
            for (List<String> messageLines : configManager.getMessages().values()) {
                String[] formattedLines = new String[messageLines.size()];
                for (int i = 0; i < messageLines.size(); i++) {
                    formattedLines[i] = ColorParser.fullFormat(messageLines.get(i), null);
                }
                formattedMessageCache.add(formattedLines);
            }
            currentMessageIndex = 0;
            Logger.debug("Message cache reloaded. Loaded " + formattedMessageCache.size() + " messages");
        } catch (Exception e) {
            Logger.error("Failed to reload message cache: " + e.getMessage());
        }
    }

    public void reload() {
        reloadMessageCache();
        startNotificationTask();
    }

    public void toggleNotifications(Player player) {
        UUID uuid = player.getUniqueId();
        if (disabledPlayers.contains(uuid)) {
            disabledPlayers.remove(uuid);
            player.sendMessage(ColorParser.fullFormat(configManager.getToggleOnMsg(), player));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 1.0f, 1.0f);
        } else {
            disabledPlayers.add(uuid);
            player.sendMessage(ColorParser.fullFormat(configManager.getToggleOffMsg(), player));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 1.0f, 0.9f);
        }
        saveDisabledPlayers();
    }

    public void disable() {
        if (notificationTask != null) {
            notificationTask.cancel();
        }
        saveDisabledPlayers();
        disabledPlayers.clear();
        formattedMessageCache.clear();
    }
}