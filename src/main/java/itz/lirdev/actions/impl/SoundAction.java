package itz.lirdev.actions.impl;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import itz.lirdev.actions.Action;

public class SoundAction implements Action {

    private final Sound sound;
    private final float volume;
    private final float pitch;

    public SoundAction(String soundName, float volume, float pitch) {
        try {
            this.sound = Sound.valueOf(soundName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown sound: " + soundName);
        }
        this.volume = volume;
        this.pitch = pitch;
    }

    public SoundAction(String soundName) {
        this(soundName, 1.0f, 1.0f);
    }

    @Override
    public void execute(Player player) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

}
