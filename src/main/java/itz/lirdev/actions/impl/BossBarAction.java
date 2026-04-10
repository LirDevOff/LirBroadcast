package itz.lirdev.actions.impl;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import itz.lirdev.actions.Action;
import itz.lirdev.tools.ColorParser;

public class BossBarAction implements Action {

    private final String title;
    private final double progress;
    private final BarColor color;
    private final BarStyle style;
    private final long duration;

    public BossBarAction(String title) {
        this(title, 1.0, BarColor.GREEN, BarStyle.SOLID, 3000);
    }

    public BossBarAction(String title, double progress, BarColor color, BarStyle style, long duration) {
        this.title = ColorParser.colorize(title);
        this.progress = Math.max(0.0, Math.min(1.0, progress));
        this.color = color;
        this.style = style;
        this.duration = duration;
    }

    @Override
    public void execute(Player player) {
        BossBar bar = Bukkit.createBossBar(title, color, style);
        bar.setProgress(progress);
        bar.addPlayer(player);

        Bukkit.getScheduler().scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugins()[0],
                () -> bar.removePlayer(player),
                duration / 50);
    }

}
