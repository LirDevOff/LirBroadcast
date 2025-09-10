package lirdev.lirbroadcast.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorParser {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String papihook(String text) {
        return papihook(text, null);
    }

    public static String papihook(String text, Player player) {
        if (text == null || !Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return text;
        }
        return PlaceholderAPI.setPlaceholders(player, text);
    }

    public static String colorize(String text) {
        if (text == null) return null;

        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder hexColor = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                hexColor.append("§").append(c);
            }
            matcher.appendReplacement(buffer, hexColor.toString());
        }
        matcher.appendTail(buffer);

        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    public static String fullFormat(String text) {
        return fullFormat(text, null);
    }

    public static String fullFormat(String text, Player player) {
        return colorize(papihook(text, player));
    }
}