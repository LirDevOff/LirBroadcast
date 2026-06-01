package itz.lirdev.actions.impl;

import org.bukkit.entity.Player;

import itz.lirdev.actions.Action;
import itz.lirdev.tools.TimeParser;

public class WaitAction implements Action {

    private final long ticks;

    public WaitAction(String content) {
        long parsed;
        try {
            parsed = TimeParser.toTicks(content.trim());
        } catch (IllegalArgumentException e) {
            parsed = 0;
        }
        this.ticks = Math.max(1, parsed);
    }

    public long getTicks() {
        return ticks;
    }

    @Override
    public void execute(Player player) {
    }
}
