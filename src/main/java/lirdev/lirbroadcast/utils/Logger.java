package lirdev.lirbroadcast.utils;

import lirdev.lirbroadcast.configuration.Config;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@UtilityClass
public class Logger {
    private String PLUGIN_NAME;
    private Config config;

    public void init(Config config, String pluginName) {
        Logger.config = config;
        Logger.PLUGIN_NAME = pluginName;
    }

    public void error(String message) {
        Bukkit.getConsoleSender().sendMessage(ColorParser.colorize("&#4EF89B[&l" + PLUGIN_NAME + "&#4EF89B] &f» &#4EF89BERROR: &f" + message));
    }

    public void critical(String message) {
        Bukkit.getConsoleSender().sendMessage(ColorParser.colorize("&#4EF89B[&l" + PLUGIN_NAME + "&#4EF89B] &f» &#4EF89BCRITICAL: &f" + message));
    }

    public void warn(String message) {
        Bukkit.getConsoleSender().sendMessage(ColorParser.colorize("&#4EF89B[&l" + PLUGIN_NAME + "&#4EF89B] &f» &#4EF89BWARN: &f" + message));
    }

    public void info(String message) {
        Bukkit.getConsoleSender().sendMessage(ColorParser.colorize("&#4EF89B[&l" + PLUGIN_NAME + "&#4EF89B] &f» &#4EF89BINFO: &f" + message));
    }

    public void msg(String message) {
        Bukkit.getConsoleSender().sendMessage(ColorParser.colorize("&#4EF89B[&l" + PLUGIN_NAME + "&#4EF89B] &f» " + message));
    }

    public void debug(String message) {
        if (config != null && config.isDebug()) {
            String text = ColorParser.colorize("&#4EF89B[&l" + PLUGIN_NAME + "&#4EF89B] &f» &#4EF89BDEBUG: &f" + message);
            Bukkit.getConsoleSender().sendMessage((text));

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission("lirbroadcast.admin") || player.isOp()) {
                    player.sendMessage(text);
                }
            }
        }
    }
}