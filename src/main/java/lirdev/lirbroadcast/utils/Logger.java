package lirdev.lirbroadcast.utils;

import org.bukkit.Bukkit;

public class Logger {

    private static final java.util.logging.Logger logger = Bukkit.getLogger();
    private static final String PLUGIN_NAME = "LirBroadcast";

    public static void error(String message) {
        logger.warning(ColorParser.colorize("&#4EF89B[" + PLUGIN_NAME + "] &#4EF89BERROR: &f" + message));
    }
    public static void critical(String message) {
        logger.warning(ColorParser.colorize("&#4EF89B[" + PLUGIN_NAME + "] &#4EF89BCRITICAL: &f" + message));
    }
    public static void warn(String message) {
        logger.warning(ColorParser.colorize("&#4EF89B[" + PLUGIN_NAME + "] &#4EF89BWARN: &f" + message));
    }
    public static void info(String message) {
        logger.warning(ColorParser.colorize("&#4EF89B[" + PLUGIN_NAME + "] &#4EF89BINFO: &f" + message));
    }
}