package itz.lirdev.actions.impl;

import org.bukkit.entity.Player;

import itz.lirdev.actions.Action;

public class WaitAction implements Action {

    private final double seconds;

    public WaitAction(double seconds) {
        if (seconds < 0) {
            throw new IllegalArgumentException("Wait time cannot be negative");
        }
        this.seconds = seconds;
    }

    @Override
    public void execute(Player player) {
    }

    public double getSeconds() {
        return seconds;
    }

    public long getTicks() {
        return Math.round(seconds * 20);
    }

}
