package itz.lirdev.actions.impl;

import org.bukkit.entity.Player;

import itz.lirdev.actions.Action;
import itz.lirdev.tools.ColorParser;

public class TitleAction implements Action {

    private final String title;
    private final String subtitle;
    private final int fadeIn;
    private final int stay;
    private final int fadeOut;

    public TitleAction(String params) {
        String[] parts = params.split("\\|");

        this.title = ColorParser.colorize(parts[0].trim());
        this.subtitle = parts.length > 1 ? ColorParser.colorize(parts[1].trim()) : "";
        this.fadeIn = parts.length > 2 ? safeParse(parts[2].trim(), 10) : 10;
        this.stay = parts.length > 3 ? safeParse(parts[3].trim(), 70) : 70;
        this.fadeOut = parts.length > 4 ? safeParse(parts[4].trim(), 20) : 20;
    }

    @Override
    public void execute(Player player) {
        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    private int safeParse(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

}
