package itz.lirdev;

import org.bukkit.plugin.java.JavaPlugin;

public class LirBroadcast extends JavaPlugin {
    
    @Override
    public void onEnable() {
        getLogger().info("LirBroadcast has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("LirBroadcast has been disabled!");
    }
    
}