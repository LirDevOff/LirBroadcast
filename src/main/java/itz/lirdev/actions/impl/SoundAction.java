package itz.lirdev.actions.impl;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import itz.lirdev.actions.Action;
import itz.lirdev.tools.Logger;

public class SoundAction implements Action {

    private final Sound sound;
    private final float volume;
    private final float pitch;

    public SoundAction(String soundName, float volume, float pitch) {
        Sound parsed = null;
        try {
            parsed = Sound.valueOf(soundName.toUpperCase());
        } catch (IllegalArgumentException e) {
            Logger.warn("Unknown sound: " + soundName);
        }
        this.sound = parsed;
        this.volume = volume;
        this.pitch = pitch;
    }

    @Override
    public void execute(Player player) {
        if (sound == null) {
            return;
        }
        player.playSound(player.getLocation(), sound, volume, pitch);
    }
}
