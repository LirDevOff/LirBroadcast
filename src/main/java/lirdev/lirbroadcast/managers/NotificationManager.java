package lirdev.lirbroadcast.managers;

import lirdev.lirbroadcast.LirBroadcast;
import lirdev.lirbroadcast.configuration.Config;
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
import java.util.stream.Collectors;

public class NotificationManager {
    private final LirBroadcast plugin;
    private final Config config;
    private final JSON jsonData;

    private final Set<UUID> disabledPlayers = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private volatile BukkitTask notificationTask;
    private final Random RANDOM = new Random();
    private int currentMessageIndex = 0;

    private final List<String> messagesIds;

    public NotificationManager(LirBroadcast plugin, Config config) {
        this.plugin = plugin;
        this.config = config;
        this.jsonData = new JSON(plugin, "disabled_players.json");
        this.messagesIds = new ArrayList<>(config.getMessages().keySet());
        initialize();
    }

    private void initialize() {
        plugin.getDataFolder().mkdirs();
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
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private void saveAsync() {
        Set<String> uuidStrings = disabledPlayers.stream()
                .map(UUID::toString)
                .collect(Collectors.toSet());
        jsonData.setAndSaveAsync("disabled_players", uuidStrings);
    }

    private void startAutoSaveTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::saveAsync, 6000L, 6000L);
    }

    private void startNotificationTask() {
        if (notificationTask != null) {
            notificationTask.cancel();
        }

        long intervalTicks = config.getInterval() * 20L;
        notificationTask = new BukkitRunnable() {
            @Override
            public void run() {
                sendBroadcastMessage();
            }
        }.runTaskTimer(plugin, 20L, intervalTicks);
    }

    private void sendBroadcastMessage() {
        if (config.getMessages().isEmpty()) {
            Logger.warn("No messages available for broadcast");
            return;
        }

        List<String> nextMessage = getNextMessage();

        boolean soundEnabled = config.isSoundEnabled();
        Sound sound = config.getSound();
        float volume = config.getVolume();
        float pitch = config.getPitch();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (disabledPlayers.contains(player.getUniqueId())) continue;
            if (nextMessage != null) {
                for (String s : nextMessage) {
                    player.sendMessage(ColorParser.setPapi(s, player));
                }
            }

            if (soundEnabled) {
                player.playSound(player.getLocation(), sound, volume, pitch);
            }
        }
    }


    private List<String> getNextMessage() {
        if (config.getMessages() == null || config.getMessages().isEmpty()) {
            return null;
        }

        if (config.isRandom()) {
            return config.getMessages().get(messagesIds.get(RANDOM.nextInt(messagesIds.size())));
        } else {
            List<String> message = config.getMessages().get(currentMessageIndex);
            currentMessageIndex = (currentMessageIndex + 1) % config.getMessages().size();
            return message;
        }
    }

    public void toggleNotifications(Player player) {
        UUID uuid = player.getUniqueId();
        if (disabledPlayers.contains(uuid)) {
            disabledPlayers.remove(uuid);
            player.sendMessage(ColorParser.setPapi(config.getToggleOnMsg(), player));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 1.0f, 1.0f);
        } else {
            disabledPlayers.add(uuid);
            player.sendMessage(ColorParser.setPapi(config.getToggleOffMsg(), player));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 1.0f, 0.9f);
        }
        saveAsync();
    }

    private void save() {
        Set<String> uuidStrings = disabledPlayers.stream()
                .map(UUID::toString)
                .collect(Collectors.toSet());
        jsonData.set("disabled_players", uuidStrings);
        jsonData.saveData();
    }

    public void disable(boolean async) {
        if (notificationTask != null) {
            notificationTask.cancel();
        }
        if (async) {
            saveAsync();
        } else {
            save();
        }
        disabledPlayers.clear();
        config.getMessages().clear();
    }
}