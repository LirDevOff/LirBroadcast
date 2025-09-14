package lirdev.lirbroadcast.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class JSON {
    private final JavaPlugin plugin;
    private final Gson gson;
    private final File dataFile;
    private final Map<String, Object> dataCache = new ConcurrentHashMap<>();

    public JSON(JavaPlugin plugin, String fileName) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        this.dataFile = new File(plugin.getDataFolder(), fileName);
        loadData();
    }

    public void loadData() {
        try {
            if (!dataFile.exists()) {
                if (dataFile.getParentFile().mkdirs()) {
                    saveData();
                }
                return;
            }

            try (FileReader reader = new FileReader(dataFile)) {
                Type type = new TypeToken<Map<String, Object>>(){}.getType();
                Map<String, Object> loadedData = gson.fromJson(reader, type);
                if (loadedData != null) {
                    dataCache.clear();
                    dataCache.putAll(loadedData);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load JSON data: " + e.getMessage());
        }
    }

    public void saveData() {
        try {
            if (!dataFile.getParentFile().exists()) {
                dataFile.getParentFile().mkdirs();
            }

            try (FileWriter writer = new FileWriter(dataFile)) {
                gson.toJson(dataCache, writer);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save JSON data: " + e.getMessage());
        }
    }

    public void saveDataAsync() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, this::saveData);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String key, Class<T> clazz, List<T> defaultValue) {
        Object value = dataCache.get(key);
        if (value instanceof List<?> list) {
            if (!list.isEmpty() && clazz.isInstance(list.get(0))) {
                return (List<T>) list;
            }
        }
        return defaultValue;
    }

    public <T> List<T> getList(String key, Class<T> clazz) {
        return getList(key, clazz, new ArrayList<>());
    }

    @SuppressWarnings("unchecked")
    public <T> Set<T> getSet(String key, Class<T> clazz, Set<T> defaultValue) {
        Object value = dataCache.get(key);
        if (value instanceof Set<?> set) {
            if (!set.isEmpty() && clazz.isInstance(set.iterator().next())) {
                return (Set<T>) set;
            }
        }
        return defaultValue;
    }

    public void set(String key, Object value) {
        dataCache.put(key, value);
    }


    public void setAndSaveAsync(String key, Object value) {
        set(key, value);
        saveDataAsync();
    }
}