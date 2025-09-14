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
                dataFile.getParentFile().mkdirs();
                saveData();
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
    public <T> T get(String key, Class<T> clazz, T defaultValue) {
        Object value = dataCache.get(key);
        if (value != null && clazz.isInstance(value)) {
            return (T) value;
        }
        return defaultValue;
    }

    public <T> T get(String key, Class<T> clazz) {
        return get(key, clazz, null);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String key, Class<T> clazz, List<T> defaultValue) {
        Object value = dataCache.get(key);
        if (value instanceof List) {
            List<?> list = (List<?>) value;
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
        if (value instanceof Set) {
            Set<?> set = (Set<?>) value;
            if (!set.isEmpty() && clazz.isInstance(set.iterator().next())) {
                return (Set<T>) set;
            }
        }
        return defaultValue;
    }

    public <T> Set<T> getSet(String key, Class<T> clazz) {
        return getSet(key, clazz, new HashSet<>());
    }

    public void set(String key, Object value) {
        dataCache.put(key, value);
    }

    public void setAndSave(String key, Object value) {
        set(key, value);
        saveData();
    }

    public void setAndSaveAsync(String key, Object value) {
        set(key, value);
        saveDataAsync();
    }

    public void remove(String key) {
        dataCache.remove(key);
    }

    public boolean contains(String key) {
        return dataCache.containsKey(key);
    }

    public Set<String> getKeys() {
        return dataCache.keySet();
    }

    public void clear() {
        dataCache.clear();
    }

    public int size() {
        return dataCache.size();
    }

    public Map<String, Object> getAll() {
        return new HashMap<>(dataCache);
    }

    @SuppressWarnings("unchecked")
    public <T> T getNested(String path, Class<T> clazz, T defaultValue) {
        String[] keys = path.split("\\.");
        Object current = dataCache;

        for (String key : keys) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(key);
            } else {
                return defaultValue;
            }
            if (current == null) {
                return defaultValue;
            }
        }

        if (clazz.isInstance(current)) {
            return (T) current;
        }
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    public void setNested(String path, Object value) {
        String[] keys = path.split("\\.");
        Map<String, Object> current = dataCache;

        for (int i = 0; i < keys.length - 1; i++) {
            String key = keys[i];
            Object next = current.get(key);

            if (!(next instanceof Map)) {
                next = new HashMap<String, Object>();
                current.put(key, next);
            }

            current = (Map<String, Object>) next;
        }

        current.put(keys[keys.length - 1], value);
    }
}