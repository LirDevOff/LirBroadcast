package itz.lirdev.tools;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import itz.lirdev.LirBroadcast;

public class FileLoader {

    private static final Map<String, CachedConfig> cache = new HashMap<>();

    public FileConfiguration getFileConfiguration(String fileName) {
        File file = new File(LirBroadcast.getInstance().getDataFolder().getAbsolutePath(), fileName);
        if (!file.exists()) {
            LirBroadcast.getInstance().saveResource(fileName, false);
        }

        long lastModified = file.lastModified();
        CachedConfig cached = cache.get(fileName);

        if (cached != null && cached.lastModified == lastModified) {
            return cached.config;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        cache.put(fileName, new CachedConfig(config, lastModified));
        return config;
    }

    public File getFile(String fileName) {
        File file = new File(LirBroadcast.getInstance().getDataFolder().getAbsoluteFile(), fileName);
        if (!file.exists()) {
            LirBroadcast.getInstance().saveResource(fileName, false);
        }
        return file;
    }

    public static void invalidate(String fileName) {
        cache.remove(fileName);
    }

    public static void invalidateAll() {
        cache.clear();
    }

    private static final class CachedConfig {

        final FileConfiguration config;
        final long lastModified;

        CachedConfig(FileConfiguration config, long lastModified) {
            this.config = config;
            this.lastModified = lastModified;
        }
    }
}
