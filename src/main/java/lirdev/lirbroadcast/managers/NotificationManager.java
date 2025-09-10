package lirdev.lirbroadcast.managers;

import lirdev.lirbroadcast.LirBroadcast;
import lirdev.lirbroadcast.utils.ColorParser;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationManager {
    private final LirBroadcast plugin;
    private final ConfigManager configManager;
    private final Set<UUID> disabledPlayers = ConcurrentHashMap.newKeySet();
    private volatile BukkitTask notificationTask;
    private final Random random = new Random();
    private volatile List<List<String>> messageCache;
    private volatile int currentMessageIndex = 0;
    private final File disabledPlayersFile;

    public NotificationManager(LirBroadcast plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.disabledPlayersFile = new File(plugin.getDataFolder(), "disabled_players.yml");
        this.messageCache = Collections.synchronizedList(new ArrayList<>());

        try {
            initialize();
        } catch (Exception e) {}
    }

    private void initialize() throws IOException {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {}

        this.messageCache = new ArrayList<>(configManager.getMessages().values());
        loadDisabledPlayers();
        startStrictIntervalTask();
        startAutoSaveTask();
    }

    private void loadDisabledPlayers() throws IOException {
        if (!disabledPlayersFile.exists()) {
            if (!disabledPlayersFile.createNewFile()) {}
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(disabledPlayersFile);
        for (String uuidString : config.getKeys(false)) {
            try {
                if (config.getBoolean(uuidString)) {
                    disabledPlayers.add(UUID.fromString(uuidString));
                }
            } catch (IllegalArgumentException e) {}
        }
    }

    private void saveDisabledPlayers() {
        try {
            YamlConfiguration config = new YamlConfiguration();
            disabledPlayers.forEach(uuid -> config.set(uuid.toString(), true));

            config.save(disabledPlayersFile);
        } catch (IOException e) {}
    }

    private void startAutoSaveTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, this::saveDisabledPlayers, 6000L, 6000L);
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
                return;
            }
        }

        List<String> messageLines = getNextMessage();
        if (messageLines.isEmpty()) return;

        boolean soundEnabled = configManager.isSoundEnabled();
        Sound sound = configManager.getSound();
        float volume = configManager.getVolume();
        float pitch = configManager.getPitch();

        int playersNotified = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (disabledPlayers.contains(player.getUniqueId())) continue;

            playersNotified++;
            for (String line : messageLines) {
                player.sendMessage(ColorParser.fullFormat(line, player));
            }

            if (soundEnabled) {
                player.playSound(player.getLocation(), sound, volume, pitch);
            }
        }
    }
    private List<String> getNextMessage() {
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

    private void reloadMessageCache() {
        this.messageCache = new ArrayList<>(configManager.getMessages().values());
        this.currentMessageIndex = 0;
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

    public boolean hasNotificationsDisabled(Player player) {
        return disabledPlayers.contains(player.getUniqueId());
    }
}