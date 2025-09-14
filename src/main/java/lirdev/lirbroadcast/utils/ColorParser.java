package lirdev.lirbroadcast.utils;

import lombok.experimental.UtilityClass;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class ColorParser {

    private final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public String setPapi(String text, Player player) {
        if (text==null) return "";
        return PlaceholderAPI.setPlaceholders(player, text);
    }
    public List<String> colorize(List<String> text) {
        List<String> strings = new ArrayList<>();
        if (text==null) return new ArrayList<>();
        for (String s : text) {
            s = colorize(s);
            strings.add(s);
        }

        return strings;
    }
    public String colorize(String text) {
        if (text == null) return null;

        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuilder buffer = new StringBuilder();

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

}