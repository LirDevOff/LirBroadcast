package itz.lirdev.actions.impl;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import itz.lirdev.actions.Action;
import itz.lirdev.tools.ColorParser;
import itz.lirdev.tools.Logger;
import itz.lirdev.tools.SchedulerUtil;

public class BossBarAction implements Action {

    private static final BarColor DEFAULT_COLOR = BarColor.GREEN;
    private static final BarStyle DEFAULT_STYLE = BarStyle.SOLID;
    private static final double DEFAULT_PROGRESS = 1.0;
    private static final int DEFAULT_DURATION = 100;

    private final JavaPlugin plugin;
    private final String rawTitle;
    private final BarColor color;
    private final BarStyle style;
    private final double progress;
    private final int durationTicks;
    private final boolean hasPapi;

    private volatile String cachedTitle;

    public BossBarAction(String content, JavaPlugin plugin) {
        this.plugin = plugin;

        String[] parts = content.split("\\|", -1);
        this.rawTitle = get(parts, 0, "").trim();
        this.hasPapi = rawTitle.contains("%");
        this.color = parseColor(get(parts, 1, ""));
        this.style = parseStyle(get(parts, 2, ""));
        this.progress = parseProgress(get(parts, 3, ""));
        this.durationTicks = parseInt(get(parts, 4, ""), DEFAULT_DURATION);
    }

    @Override
    public void execute(Player player) {
        String text = hasPapi
                ? ColorParser.setPapi(cachedTitle(), player)
                : cachedTitle();

        BossBar bar = Bukkit.createBossBar(text, color, style);
        bar.setProgress(progress);
        bar.addPlayer(player);

        SchedulerUtil.runTaskLaterForPlayer(plugin, player, () -> {
            bar.removePlayer(player);
            bar.setVisible(false);
            bar.setTitle("");
        }, durationTicks);
    }

    private String cachedTitle() {
        String local = cachedTitle;
        if (local == null) {
            local = ColorParser.colorize(rawTitle);
            cachedTitle = local;
        }
        return local;
    }

    private static BarColor parseColor(String value) {
        try {
            return BarColor.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            if (!value.isBlank()) {
                Logger.warn("Unknown BossBar color \"" + value.trim() + "\", using default");
            }
            return DEFAULT_COLOR;
        }
    }

    private static BarStyle parseStyle(String value) {
        try {
            return BarStyle.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            if (!value.isBlank()) {
                Logger.warn("Unknown BossBar style \"" + value.trim() + "\", using default");
            }
            return DEFAULT_STYLE;
        }
    }

    private static double parseProgress(String value) {
        if (value.isBlank()) {
            return DEFAULT_PROGRESS;
        }
        try {
            double p = Double.parseDouble(value.trim());
            if (p < 0.0 || p > 1.0) {
                Logger.warn("BossBar progress out of range, using default");
                return DEFAULT_PROGRESS;
            }
            return p;
        } catch (NumberFormatException e) {
            return DEFAULT_PROGRESS;
        }
    }

    private static int parseInt(String value, int def) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static String get(String[] arr, int index, String def) {
        return (index < arr.length) ? arr[index] : def;
    }
}
