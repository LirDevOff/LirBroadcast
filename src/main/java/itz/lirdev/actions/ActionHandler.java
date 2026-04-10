package itz.lirdev.actions;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import itz.lirdev.actions.impl.ActionBarAction;
import itz.lirdev.actions.impl.BossBarAction;
import itz.lirdev.actions.impl.MessageAction;
import itz.lirdev.actions.impl.SoundAction;
import itz.lirdev.actions.impl.TitleAction;
import itz.lirdev.actions.impl.WaitAction;
import itz.lirdev.tools.Logger;

public class ActionHandler {

    private final JavaPlugin plugin;

    public ActionHandler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public List<Action> parseActions(List<String> actionLines) {
        List<Action> actions = new ArrayList<>();

        for (String line : actionLines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }

            try {
                Action action = parseAction(line);
                if (action != null) {
                    actions.add(action);
                }
            } catch (Exception e) {
                Logger.error("Failed to parse action: " + line + " - " + e.getMessage());
            }
        }

        return actions;
    }

    private Action parseAction(String line) {
        if (line.startsWith("[message]")) {
            String content = line.substring("[message]".length());
            if (content.startsWith(" ") && content.length() > 1) {
                content = content.substring(1);
            }
            return new MessageAction(content);
        }

        if (line.startsWith("[title]")) {
            String content = line.substring("[title]".length()).trim();
            return new TitleAction(content);
        }

        if (line.startsWith("[bossbar]")) {
            String content = line.substring("[bossbar]".length()).trim();
            return new BossBarAction(content);
        }

        if (line.startsWith("[sound]")) {
            String content = line.substring("[sound]".length()).trim();
            String[] parts = content.split("\\s+");
            if (parts.length >= 1) {
                float volume = parts.length > 1 ? safeParse(parts[1], 1.0f) : 1.0f;
                float pitch = parts.length > 2 ? safeParse(parts[2], 1.0f) : 1.0f;
                return new SoundAction(parts[0], volume, pitch);
            }
            return new SoundAction(content);
        }

        if (line.startsWith("[wait]")) {
            String content = line.substring("[wait]".length()).trim();
            double seconds = Double.parseDouble(content);
            return new WaitAction(seconds);
        }

        if (line.startsWith("[actionbar]")) {
            String content = line.substring("[actionbar]".length()).trim();
            return new ActionBarAction(content);
        }

        return null;
    }

    private float safeParse(String value, float defaultValue) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public void executeActions(Player player, List<Action> actions) {
        if (actions.isEmpty()) {
            return;
        }

        executeActionsAsync(player, actions, 0);
    }

    private void executeActionsAsync(Player player, List<Action> actions, int index) {
        if (index >= actions.size()) {
            return;
        }

        Action action = actions.get(index);

        if (action instanceof WaitAction) {
            WaitAction wait = (WaitAction) action;
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                executeActionsAsync(player, actions, index + 1);
            }, wait.getTicks());
        } else {
            action.execute(player);
            executeActionsAsync(player, actions, index + 1);
        }
    }

}
