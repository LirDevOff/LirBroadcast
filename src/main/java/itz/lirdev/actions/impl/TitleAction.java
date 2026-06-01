package itz.lirdev.actions.impl;

import java.time.Duration;

import org.bukkit.entity.Player;

import itz.lirdev.actions.Action;
import itz.lirdev.tools.ColorParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

public class TitleAction implements Action {

    private final String rawTitle;
    private final String rawSub;
    private final Title.Times times;
    private final boolean hasPapiTitle;
    private final boolean hasPapiSub;

    private volatile Component cachedTitle;
    private volatile Component cachedSub;

    public TitleAction(String content) {
        String[] parts = content.split("\\|", -1);

        this.rawTitle = get(parts, 0, "").trim();
        this.rawSub = get(parts, 1, "").trim();

        this.hasPapiTitle = rawTitle.contains("%");
        this.hasPapiSub = rawSub.contains("%");

        int fadeIn = parseInt(get(parts, 2, ""), 10);
        int stay = parseInt(get(parts, 3, ""), 70);
        int fadeOut = parseInt(get(parts, 4, ""), 20);

        this.times = Title.Times.of(
                Duration.ofMillis(fadeIn * 50L),
                Duration.ofMillis(stay * 50L),
                Duration.ofMillis(fadeOut * 50L)
        );
    }

    @Override
    public void execute(Player player) {
        Component title = hasPapiTitle
                ? ColorParser.toComponent(ColorParser.setPapi(rawTitle, player))
                : cachedTitle();

        Component sub = hasPapiSub
                ? ColorParser.toComponent(ColorParser.setPapi(rawSub, player))
                : cachedSub();

        player.showTitle(Title.title(title, sub, times));
    }

    private Component cachedTitle() {
        Component local = cachedTitle;
        if (local == null) {
            local = ColorParser.toComponent(rawTitle);
            cachedTitle = local;
        }
        return local;
    }

    private Component cachedSub() {
        Component local = cachedSub;
        if (local == null) {
            local = ColorParser.toComponent(rawSub);
            cachedSub = local;
        }
        return local;
    }

    private static String get(String[] arr, int i, String def) {
        return i < arr.length ? arr[i] : def;
    }

    private static int parseInt(String v, int def) {
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
