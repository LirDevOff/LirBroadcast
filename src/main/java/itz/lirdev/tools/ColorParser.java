package itz.lirdev.tools;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import itz.lirdev.configuration.Config;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ColorParser {

    private static final MiniMessage MINI_MESSAGE;
    private static final boolean MINI_MESSAGE_ENABLED;
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    static {
        MiniMessage mm = null;
        try {
            Method method = MiniMessage.class.getMethod("miniMessage");
            mm = (MiniMessage) method.invoke(null);
        } catch (Throwable ignored) {
        }
        MINI_MESSAGE = mm;
        MINI_MESSAGE_ENABLED = (mm != null);
    }

    private static final int HEX_CACHE_MAX = 512;
    private static final int TEXT_CACHE_MAX = 1024;
    private static final Map<String, String> HEX_CACHE = boundedLruCache(HEX_CACHE_MAX);
    private static final Map<String, String> COLORIZE_CACHE = boundedLruCache(TEXT_CACHE_MAX);
    private static final Map<String, Component> COMPONENT_CACHE = boundedLruCache(TEXT_CACHE_MAX);

    private static final String[] MM_TAGS = {
            "</", "<#", "<gradient", "<rainbow",
            "<bold>", "<italic>", "<underlined>", "<strikethrough>", "<obfuscated>", "<reset>",
            "<red>", "<green>", "<blue>", "<yellow>", "<white>", "<black>",
            "<gray>", "<grey>", "<gold>", "<aqua>",
            "<dark_", "<light_",
            "<", "<click", "<insertion", "<font", "<newline>",
            "<lang", "<key", "<score", "<selector", "<transition"
    };

    private static Config config;
    private static boolean papiEnabled;

    private static <V> Map<String, V> boundedLruCache(int maxSize) {
        Map<String, V> map = new LinkedHashMap<>(64, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, V> eldest) {
                return size() > maxSize;
            }
        };
        return Collections.synchronizedMap(map);
    }

    public static void setConfig(Config cfg) {
        config = cfg;
    }

    public static void setPapiEnabled(boolean enabled) {
        papiEnabled = enabled;
    }

    public static boolean isMiniMessageEnabled() {
        return MINI_MESSAGE_ENABLED;
    }

    public static void clearCache() {
        COLORIZE_CACHE.clear();
        COMPONENT_CACHE.clear();
    }

    public static String setPapi(String text, Player player) {
        if (!papiEnabled || text == null || player == null) {
            return text;
        }

        if (text.indexOf('%') == -1) {
            return text;
        }
        return PlaceholderAPI.setPlaceholders(player, text);
    }

    public static List<String> colorize(List<String> list) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> out = new ArrayList<>(list.size());
        for (String s : list) {
            out.add(colorize(s));
        }
        return out;
    }

    public static List<String> colorize(List<String> list, Player player) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> out = new ArrayList<>(list.size());
        for (String s : list) {
            out.add(colorize(setPapi(s, player)));
        }
        return out;
    }

    public static String colorize(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String cached = COLORIZE_CACHE.get(text);
        if (cached != null) {
            return cached;
        }

        String prefixed = applyPrefix(text);
        String result;
        if (MINI_MESSAGE_ENABLED && containsMiniMessageTag(prefixed)) {
            result = LEGACY.serialize(MINI_MESSAGE.deserialize(convertLegacyToMM(prefixed)));
        } else {
            result = legacyColorize(prefixed);
        }

        COLORIZE_CACHE.put(text, result);
        return result;
    }

    public static Component toComponent(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        Component cached = COMPONENT_CACHE.get(text);
        if (cached != null) {
            return cached;
        }

        String prefixed = applyPrefix(text);
        Component result;
        if (MINI_MESSAGE_ENABLED && containsMiniMessageTag(prefixed)) {
            result = MINI_MESSAGE.deserialize(convertLegacyToMM(prefixed));
        } else {
            result = LEGACY.deserialize(legacyColorize(prefixed));
        }

        COMPONENT_CACHE.put(text, result);
        return result;
    }

    public static Component toComponent(String text, Player player) {
        return toComponent(setPapi(text, player));
    }

    private static String applyPrefix(String text) {
        if (config != null && text.indexOf('{') != -1) {
            return text.replace("{prefix}", config.getPrefix());
        }
        return text;
    }

    public static boolean containsMiniMessageTag(String text) {
        if (text.indexOf('<') == -1) {
            return false;
        }
        for (String tag : MM_TAGS) {
            if (text.contains(tag)) {
                return true;
            }
        }
        return false;
    }

    private static String convertLegacyToMM(String text) {
        if (text.indexOf('&') == -1) {
            return text;
        }

        StringBuilder sb = new StringBuilder(text.length() + 32);
        int len = text.length();

        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);

            if (c == '&' && i + 7 < len && text.charAt(i + 1) == '#') {
                String hex = text.substring(i + 2, i + 8);
                if (isHex(hex)) {
                    sb.append("<#").append(hex.toUpperCase()).append('>');
                    i += 7;
                    continue;
                }
            }

            if (c == '&' && i + 1 < len) {
                String tag = legacyToMMTag(Character.toLowerCase(text.charAt(i + 1)));
                if (tag != null) {
                    sb.append(tag);
                    i++;
                    continue;
                }
            }

            sb.append(c);
        }

        return sb.toString();
    }

    private static boolean isHex(String s) {
        for (int i = 0; i < s.length(); i++) {
            char h = s.charAt(i);
            if (!((h >= '0' && h <= '9') || (h >= 'a' && h <= 'f') || (h >= 'A' && h <= 'F'))) {
                return false;
            }
        }
        return true;
    }

    private static String legacyColorize(String text) {
        if (text.indexOf('&') == -1) {
            return text;
        }

        if (text.contains("&#")) {
            text = replaceHex(text);
        }

        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private static String replaceHex(String text) {
        int idx = text.indexOf("&#");
        if (idx == -1) {
            return text;
        }

        StringBuilder sb = new StringBuilder(text.length() + 32);
        int last = 0;

        while (idx != -1 && idx + 8 <= text.length()) {
            String hex = text.substring(idx + 2, idx + 8);
            if (isHex(hex)) {
                sb.append(text, last, idx);
                String cached = HEX_CACHE.get(hex);
                if (cached == null) {
                    cached = buildHexColor(hex.toUpperCase());
                    HEX_CACHE.put(hex, cached);
                }
                sb.append(cached);
                last = idx + 8;
                idx = text.indexOf("&#", last);
            } else {
                idx = text.indexOf("&#", idx + 1);
            }
        }

        sb.append(text, last, text.length());
        return sb.toString();
    }

    private static String buildHexColor(String hex) {
        StringBuilder sb = new StringBuilder(14);
        sb.append("§x");
        for (int i = 0; i < 6; i++) {
            sb.append('§').append(hex.charAt(i));
        }
        return sb.toString();
    }

    private static String legacyToMMTag(char code) {
        return switch (code) {
            case '0' ->
                "<black>";
            case '1' ->
                "<dark_blue>";
            case '2' ->
                "<dark_green>";
            case '3' ->
                "<dark_aqua>";
            case '4' ->
                "<dark_red>";
            case '5' ->
                "<dark_purple>";
            case '6' ->
                "<gold>";
            case '7' ->
                "<gray>";
            case '8' ->
                "<dark_gray>";
            case '9' ->
                "<blue>";
            case 'a' ->
                "<green>";
            case 'b' ->
                "<aqua>";
            case 'c' ->
                "<red>";
            case 'd' ->
                "<light_purple>";
            case 'e' ->
                "<yellow>";
            case 'f' ->
                "<white>";
            case 'k' ->
                "<obfuscated>";
            case 'l' ->
                "<bold>";
            case 'm' ->
                "<strikethrough>";
            case 'n' ->
                "<underlined>";
            case 'o' ->
                "<italic>";
            case 'r' ->
                "<reset>";
            default ->
                null;
        };
    }
}