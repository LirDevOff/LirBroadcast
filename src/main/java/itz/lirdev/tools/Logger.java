package itz.lirdev.tools;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import itz.lirdev.configuration.Config;

public class Logger {

    private static String PLUGIN_NAME;
    private static Config config;

    private static String PREFIX_ERROR;
    private static String PREFIX_CRITICAL;
    private static String PREFIX_WARN;
    private static String PREFIX_INFO;
    private static String PREFIX_DEBUG;

    public static void init(Config cfg, String pluginName) {
        config = cfg;
        PLUGIN_NAME = pluginName;

        String base = "&#4EF89B[&l" + pluginName + "&#4EF89B] &f→&r ";
        PREFIX_ERROR = ColorParser.colorize(base + "ERROR: &f");
        PREFIX_CRITICAL = ColorParser.colorize(base + "CRITICAL: &f");
        PREFIX_WARN = ColorParser.colorize(base + "WARN: &f");
        PREFIX_INFO = ColorParser.colorize(base + "INFO: &f");
        PREFIX_DEBUG = ColorParser.colorize(base + "DEBUG: &f");
    }

    public static void error(String message) {
        Bukkit.getConsoleSender().sendMessage(PREFIX_ERROR + message);
    }

    public static void critical(String message) {
        Bukkit.getConsoleSender().sendMessage(PREFIX_CRITICAL + message);
    }

    public static void warn(String message) {
        Bukkit.getConsoleSender().sendMessage(PREFIX_WARN + message);
    }

    public static void info(String message) {
        Bukkit.getConsoleSender().sendMessage(PREFIX_INFO + message);
    }

    public static void msg(String message) {
        Bukkit.getConsoleSender().sendMessage(ColorParser.colorize(message));
    }

    public static void debug(String message) {
        if (config == null || !config.isDebug()) {
            return;
        }

        String text = PREFIX_DEBUG + message;
        Bukkit.getConsoleSender().sendMessage(text);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("lirbroadcast.admin") || player.isOp()) {
                player.sendMessage(text);
            }
        }
    }
}
