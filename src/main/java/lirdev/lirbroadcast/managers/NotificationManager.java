package lirdev.lirbroadcast.managers;

import lirdev.lirbroadcast.LirBroadcast;
import lirdev.lirbroadcast.utils.ColorParser;
import lirdev.lirbroadcast.utils.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationManager {
    private final LirBroadcast plugin;
    private final ConfigManager configManager;

    private final Set<UUID> disabledPlayers = ConcurrentHashMap.newKeySet();
    private volatile BukkitTask notificationTask;
    private final Random random = new Random();
    private List<List<String>> messageCache;

    private int currentMessageIndex = 0;

    private final File disabledPlayersFile;
    private final Gson gson;

    public NotificationManager(LirBroadcast plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.disabledPlayersFile = new File(plugin.getDataFolder(), "disabled_players.json");
        this.messageCache = new ArrayList<>();
        this.gson = new GsonBuilder().setPrettyPrinting().create();

        try {
            initialize();
        } catch (Exception e) {
            Logger.debug("Failed to initialize NotificationManager: " + e.getMessage());
        }
    }

    private void initialize() throws IOException {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {}

        reloadMessageCache();
        loadDisabledPlayers();
        startStrictIntervalTask();
        startAutoSaveTask();
    }

    private void loadDisabledPlayers() {
        try {
            if (!disabledPlayersFile.exists()) {
                saveDisabledPlayers();
                return;
            }

            try (FileReader reader = new FileReader(disabledPlayersFile)) {
                Type type = new TypeToken<Set<String>>(){}.getType();
                Set<String> uuidStrings = gson.fromJson(reader, type);

                if (uuidStrings != null) {
                    for (String uuidString : uuidStrings) {
                        try {
                            disabledPlayers.add(UUID.fromString(uuidString));
                        } catch (IllegalArgumentException e) {}
                    }
                }
            }
        } catch (Exception e) {
            Logger.error("Failed to load disabled players: " + e.getMessage());
        }
    }

    private void saveDisabledPlayers() {
        try {
            if (!disabledPlayersFile.getParentFile().exists()) {
                if (!disabledPlayersFile.getParentFile().mkdirs()) {
                    return;
                }
            }

            try (FileWriter writer = new FileWriter(disabledPlayersFile)) {
                Set<String> uuidStrings = new HashSet<>();
                for (UUID uuid : disabledPlayers) {
                    uuidStrings.add(uuid.toString());
                }
                gson.toJson(uuidStrings, writer);
            }
        } catch (Exception e) {}
    }

    private void startAutoSaveTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::saveDisabledPlayers, 6000L, 6000L);
    }

    private void startStrictIntervalTask() {
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
        if (messageCache.isEmpty()) {
            reloadMessageCache();
            if (messageCache.isEmpty()) {
                Logger.warn("No messages available for broadcast");
                return;
            }
        }

        List<String> messageLines = getNextMessage();
        if (messageLines.isEmpty()) {
            return;
        }

        boolean soundEnabled = configManager.isSoundEnabled();
        Sound sound = configManager.getSound();
        float volume = configManager.getVolume();
        float pitch = configManager.getPitch();

        String[] formattedMessages = new String[messageLines.size()];
        for (int i = 0; i < messageLines.size(); i++) {
            formattedMessages[i] = ColorParser.fullFormat(messageLines.get(i), null);
        }

        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();

        for (Player player : onlinePlayers) {
            if (disabledPlayers.contains(player.getUniqueId())) continue;

            for (String message : formattedMessages) {
                player.sendMessage(message);
            }

            if (soundEnabled) {
                player.playSound(player.getLocation(), sound, volume, pitch);
            }
        }
    }

    private synchronized List<String> getNextMessage() {
        if (messageCache.isEmpty()) {
            return Collections.emptyList();
        }

        if (configManager.isRandom()) {
            return messageCache.get(random.nextInt(messageCache.size()));
        } else {
            List<String> message = messageCache.get(currentMessageIndex);
            currentMessageIndex = (currentMessageIndex + 1) % messageCache.size();
            return message;
        }
    }

    private synchronized void reloadMessageCache() {
        try {
            Collection<List<String>> messages = configManager.getMessages().values();
            messageCache = new ArrayList<>(messages);
            currentMessageIndex = 0;
            Logger.debug("Message cache reloaded. Loaded " + messages.size() + " messages");
        } catch (Exception e) {}
    }

    public void reload() {
        reloadMessageCache();
        startStrictIntervalTask();
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
            notificationTask = null;
        }
        saveDisabledPlayers();
        disabledPlayers.clear();
        messageCache.clear();
    }
}