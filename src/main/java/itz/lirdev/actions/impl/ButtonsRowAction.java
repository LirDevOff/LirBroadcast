package itz.lirdev.actions.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;

import itz.lirdev.actions.Action;
import itz.lirdev.tools.ColorParser;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class ButtonsRowAction implements Action {

    private static final Pattern SEP = Pattern.compile("\\s*;;\\s*");

    private final String raw;
    private final boolean hasPapi;

    public ButtonsRowAction(String content) {
        this.raw = content;
        this.hasPapi = content.contains("%");
    }

    @Override
    public void execute(Player player) {
        String processed = hasPapi ? ColorParser.setPapi(raw, player) : raw;
        String[] segments = SEP.split(processed);

        List<BaseComponent> components = new ArrayList<>();

        for (int i = 0; i < segments.length; i++) {
            String seg = segments[i];

            if (seg.contains("|")) {
                components.add(buildButton(seg));
            } else {
                for (BaseComponent bc : TextComponent.fromLegacyText(ColorParser.colorize(seg))) {
                    components.add(bc);
                }
            }

            if (i < segments.length - 1) {
                components.add(new TextComponent(" "));
            }
        }

        player.spigot().sendMessage(components.toArray(new BaseComponent[0]));
    }

    private BaseComponent buildButton(String segment) {
        String[] parts = segment.split("\\|", 3);

        String label = ColorParser.colorize(parts[0]);
        String action = parts.length > 1 ? parts[1].trim() : "";
        String hover = parts.length > 2 ? ColorParser.colorize(parts[2]) : label;

        TextComponent btn = new TextComponent(TextComponent.fromLegacyText(label));

        if (!action.isEmpty()) {
            ClickEvent.Action clickType = (action.startsWith("http://") || action.startsWith("https://"))
                    ? ClickEvent.Action.OPEN_URL
                    : ClickEvent.Action.RUN_COMMAND;
            btn.setClickEvent(new ClickEvent(clickType, action));
        }
        btn.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new Text(TextComponent.fromLegacyText(hover))
        ));

        return btn;
    }
}
