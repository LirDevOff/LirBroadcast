package itz.lirdev.configuration;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Config {

    private final JavaPlugin plugin;
    private final FileConfiguration configuration;

    public Config(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configuration = plugin.getConfig();
        load();
    }

    public void load() {

    }
}
