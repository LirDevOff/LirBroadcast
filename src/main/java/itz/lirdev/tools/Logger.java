package itz.lirdev.tools;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import itz.lirdev.configuration.Config;

public class Logger {

    private static String PLUGIN_NAME;
    private static Config config;

    public static void init(Config config, String pluginName) {
        Logger.config = config;
        Logger.PLUGIN_NAME = pluginName;
    }

    public static void error(String message) {
        Bukkit.getConsoleSender().sendMessage(ColorParser.colorize("&#4EF89B[&l" + PLUGIN_NAME + "&#4EF89B] &f→&r &#4EF89BERROR: &f" + message));
    }

    public static void critical(String message) {
        Bukkit.getConsoleSender().sendMessage(ColorParser.colorize("&#4EF89B[&l" + PLUGIN_NAME + "&#4EF89B] &f→&r &#4EF89BCRITICAL: &f" + message));
    }

    public static void warn(String message) {
        Bukkit.getConsoleSender().sendMessage(ColorParser.colorize("&#4EF89B[&l" + PLUGIN_NAME + "&#4EF89B] &f→&r &#4EF89BWARN: &f" + message));
    }

    public static void info(String message) {
        Bukkit.getConsoleSender().sendMessage(ColorParser.colorize("&#4EF89B[&l" + PLUGIN_NAME + "&#4EF89B] &f→&r &#4EF89BINFO: &f" + message));
    }

    public static void msg(String message) {
        Bukkit.getConsoleSender().sendMessage(ColorParser.colorize(message));
    }

    public static void debug(String message) {
        if (config != null && config.isDebug()) {
            String text = ColorParser.colorize("&#4EF89B[&l" + PLUGIN_NAME + "&#4EF89B] &f→&r &#4EF89BDEBUG: &f" + message);
            Bukkit.getConsoleSender().sendMessage((text));

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission("lirbroadcast.admin") || player.isOp()) {
                    player.sendMessage(text);
                }
            }
        }
    }
}
