package itz.lirdev.actions.impl;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import itz.lirdev.actions.Action;
import itz.lirdev.tools.Logger;

public class EffectAction implements Action {

    private static final int DEFAULT_DURATION_TICKS = 600;
    private static final int DEFAULT_AMPLIFIER = 0;

    private final PotionEffect effect;

    public EffectAction(String content) {
        String[] parts = content.split("\\s+", 3);
        String effectName = parts[0].trim().toUpperCase();
        PotionEffectType type = PotionEffectType.getByName(effectName);

        if (type == null) {
            Logger.warn("Unknown potion effect: " + effectName);
            this.effect = null;
            return;
        }

        int duration = parts.length > 1
                ? parseInt(parts[1], DEFAULT_DURATION_TICKS / 20) * 20
                : DEFAULT_DURATION_TICKS;
        int amplifier = parts.length > 2
                ? parseInt(parts[2], DEFAULT_AMPLIFIER)
                : DEFAULT_AMPLIFIER;

        this.effect = new PotionEffect(type, duration, amplifier);
    }

    @Override
    public void execute(Player player) {
        if (effect == null) {
            return;
        }
        player.addPotionEffect(effect);
    }

    private static int parseInt(String value, int def) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
