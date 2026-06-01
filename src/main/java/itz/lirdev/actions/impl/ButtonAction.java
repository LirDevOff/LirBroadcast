package itz.lirdev.actions.impl;

import org.bukkit.entity.Player;

import itz.lirdev.actions.Action;
import itz.lirdev.tools.ColorParser;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class ButtonAction implements Action {

    public enum Mode {
        RUN_COMMAND(ClickEvent.Action.RUN_COMMAND),
        OPEN_URL(ClickEvent.Action.OPEN_URL),
        SUGGEST_COMMAND(ClickEvent.Action.SUGGEST_COMMAND);

        final ClickEvent.Action clickAction;

        Mode(ClickEvent.Action clickAction) {
            this.clickAction = clickAction;
        }
    }

    private final Mode mode;
    private final boolean hasPapi;

    private final String raw;
    private volatile String cachedLabel;
    private volatile String cachedAction;
    private volatile String cachedHover;

    public ButtonAction(Mode mode, String content) {
        this.mode = mode;
        this.raw = content;
        this.hasPapi = content.contains("%");
    }

    @Override
    public void execute(Player player) {
        if (hasPapi) {
            sendButton(player, ColorParser.setPapi(raw, player));
        } else {
            sendCached(player);
        }
    }

    private void sendCached(Player player) {
        if (cachedLabel == null) {
            String[] parts = raw.split("\\|", 3);
            cachedLabel = ColorParser.colorize(parts[0]);
            cachedAction = parts.length > 1 ? parts[1].trim() : "";
            cachedHover = parts.length > 2 ? ColorParser.colorize(parts[2]) : cachedLabel;
        }
        player.spigot().sendMessage(buildComponent(cachedLabel, cachedAction, cachedHover));
    }

    private void sendButton(Player player, String processed) {
        String[] parts = processed.split("\\|", 3);
        String label = ColorParser.colorize(parts[0]);
        String action = parts.length > 1 ? parts[1].trim() : "";
        String hover = parts.length > 2 ? ColorParser.colorize(parts[2]) : label;
        player.spigot().sendMessage(buildComponent(label, action, hover));
    }

    private TextComponent buildComponent(String label, String action, String hover) {
        TextComponent btn = new TextComponent(TextComponent.fromLegacyText(label));

        if (!action.isEmpty()) {
            btn.setClickEvent(new ClickEvent(mode.clickAction, action));
        }
        btn.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new Text(TextComponent.fromLegacyText(hover))
        ));
        return btn;
    }
}
