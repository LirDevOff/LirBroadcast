package itz.lirdev.actions.impl;

import org.bukkit.entity.Player;

import itz.lirdev.actions.Action;
import itz.lirdev.tools.ColorParser;
import net.kyori.adventure.text.Component;

public class ActionBarAction implements Action {

    private final String raw;
    private final boolean hasPapi;
    private volatile Component cachedComponent;

    public ActionBarAction(String content) {
        this.raw = content;
        this.hasPapi = content.contains("%");
    }

    @Override
    public void execute(Player player) {
        player.sendActionBar(component(player));
    }

    private Component component(Player player) {
        if (hasPapi) {
            return ColorParser.toComponent(ColorParser.setPapi(raw, player));
        }

        Component local = cachedComponent;
        if (local == null) {
            local = ColorParser.toComponent(raw);
            cachedComponent = local;
        }
        return local;
    }
}
