package itz.lirdev.configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import itz.lirdev.LirBroadcast;
import itz.lirdev.tools.Logger;

public class ConfigMigrator {

    public static final int CURRENT_VERSION = 1;

    private final File userFile;

    public ConfigMigrator(File userFile) {
        this.userFile = userFile;
    }

    public boolean migrate(FileConfiguration userConfig) {
        int userVersion = userConfig.getInt("config-version", 0);

        if (userVersion >= CURRENT_VERSION) {
            return false;
        }

        Logger.warn("Config is outdated (version " + userVersion + " → " + CURRENT_VERSION + "). Migrating...");

        FileConfiguration defaultConfig = loadDefaultConfig();
        if (defaultConfig == null) {
            return false;
        }

        boolean changed = false;

        for (String key : defaultConfig.getKeys(true)) {
            if (!userConfig.contains(key)) {
                userConfig.set(key, defaultConfig.get(key));
                Logger.info("  + Added missing key: " + key);
                changed = true;
            }
        }

        userConfig.set("config-version", CURRENT_VERSION);
        changed = true;

        if (changed) {
            save(userConfig);
        }

        return changed;
    }

    private FileConfiguration loadDefaultConfig() {
        InputStream stream = LirBroadcast.getInstance().getResource("config.yml");
        if (stream == null) {
            return null;
        }
        return YamlConfiguration.loadConfiguration(
                new InputStreamReader(stream, StandardCharsets.UTF_8));
    }

    private void save(FileConfiguration config) {
        try {
            config.save(userFile);
        } catch (IOException e) {
            Logger.debug("Failed to save migrated config: " + e.getMessage());
        }
    }
}
