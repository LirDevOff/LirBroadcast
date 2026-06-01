package itz.lirdev.actions.impl;

import org.bukkit.entity.Player;

import itz.lirdev.actions.Action;
import itz.lirdev.tools.ChatCenterParser;
import itz.lirdev.tools.ColorParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class MessageAction implements Action {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private enum Align {
        LEFT, CENTER, RIGHT
    }

    private final String raw;
    private final Align align;
    private final boolean hasPapi;

    private volatile Component cachedComponent;

    public MessageAction(String content) {
        this.align = detectAlign(content);
        this.raw = extractContent(content, align);
        this.hasPapi = raw.contains("%");
    }

    @Override
    public void execute(Player player) {
        if (hasPapi) {
            player.sendMessage(buildComponent(ColorParser.setPapi(raw, player)));
        } else {
            Component local = cachedComponent;
            if (local == null) {
                local = buildComponent(raw);
                cachedComponent = local;
            }
            player.sendMessage(local);
        }
    }

    private Component buildComponent(String text) {
        return switch (align) {
            case CENTER ->
                LEGACY.deserialize(ChatCenterParser.center(ColorParser.colorize(text)));
            case RIGHT ->
                LEGACY.deserialize(ChatCenterParser.right(ColorParser.colorize(text)));
            case LEFT ->
                ColorParser.toComponent(text);
        };
    }

    private static Align detectAlign(String text) {
        String lower = text.toLowerCase();
        if (lower.contains("<center>")) {
            return Align.CENTER;
        }
        if (lower.contains("<right>")) {
            return Align.RIGHT;
        }
        return Align.LEFT;
    }

    private static String extractContent(String text, Align align) {
        return switch (align) {
            case CENTER ->
                strip(text, "<center>", "</center>");
            case RIGHT ->
                strip(text, "<right>", "</right>");
            case LEFT ->
                strip(text, "<left>", "</left>");
        };
    }

    private static String strip(String text, String open, String close) {
        String lower = text.toLowerCase();
        int start = lower.indexOf(open);
        int end = lower.indexOf(close);

        if (start == -1 && end == -1) {
            return text;
        }

        String before = start >= 0 ? text.substring(0, start) : "";
        String content;
        String after = "";

        if (start >= 0 && end > start) {
            content = text.substring(start + open.length(), end);
            after = text.substring(end + close.length());
        } else if (start >= 0) {
            content = text.substring(start + open.length());
        } else {
            content = text.substring(0, end);
            after = text.substring(end + close.length());
        }

        return before + content + after;
    }
}
