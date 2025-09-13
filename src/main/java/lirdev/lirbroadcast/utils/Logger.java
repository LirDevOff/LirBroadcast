package lirdev.lirbroadcast.utils;

import lirdev.lirbroadcast.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Logger {

    private static final java.util.logging.Logger logger = Bukkit.getLogger();
    private static final String PLUGIN_NAME = "LirBroadcast";
    private static ConfigManager configManager;

    public static void init(ConfigManager configManager) {
        Logger.configManager = configManager;
    }

    public static void error(String message) {
        logger.warning(ColorParser.colorize("&#4EF89B[&l" + PLUGIN_NAME + "&#4EF89B] &f» &#4EF89BERROR: &f" + message));
    }
    public static void critical(String message) {
        logger.warning(ColorParser.colorize("&#4EF89B[&l" + PLUGIN_NAME + "&#4EF89B] &f» &#4EF89BCRITICAL: &f" + message));
    }
    public static void warn(String message) {
        logger.warning(ColorParser.colorize("&#4EF89B[&l" + PLUGIN_NAME + "&#4EF89B] &f» &#4EF89BWARN: &f" + message));
    }
    public static void info(String message) {
        logger.warning(ColorParser.colorize("&#4EF89B[&l" + PLUGIN_NAME + "&#4EF89B] &f» &#4EF89BINFO: &f" + message));
    }

    public static void debug(String message) {
        if (configManager != null && configManager.isDebug()) {
            String text = ColorParser.colorize("&#4EF89B[&l" + PLUGIN_NAME + "&#4EF89B] &f» &#4EF89BDEBUG: &f" + message);
            logger.warning(text);

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission("lirbroadcast.admin")) {
                    player.sendMessage(text);
                }
            }
        }
    }
}