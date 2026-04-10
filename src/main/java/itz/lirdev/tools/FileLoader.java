package itz.lirdev.tools;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import itz.lirdev.LirBroadcast;

public class FileLoader {

    public FileConfiguration getFileConfiguration(String fileName) {
        File file = new File(LirBroadcast.getInstance().getDataFolder().getAbsolutePath(), fileName);
        if (!file.exists()) {
            LirBroadcast.getInstance().saveResource(fileName, false);

        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public File getFile(String fileName) {
        File file = new File(LirBroadcast.getInstance().getDataFolder().getAbsoluteFile(), fileName);
        if (!file.exists()) {
            LirBroadcast.getInstance().saveResource(fileName, false);
        }
        return file;
    }

}
