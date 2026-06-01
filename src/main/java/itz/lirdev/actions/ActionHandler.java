package itz.lirdev.actions;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import itz.lirdev.actions.impl.ActionBarAction;
import itz.lirdev.actions.impl.BossBarAction;
import itz.lirdev.actions.impl.ButtonAction;
import itz.lirdev.actions.impl.ButtonsRowAction;
import itz.lirdev.actions.impl.ConsoleAction;
import itz.lirdev.actions.impl.EffectAction;
import itz.lirdev.actions.impl.MessageAction;
import itz.lirdev.actions.impl.PlayerAction;
import itz.lirdev.actions.impl.SoundAction;
import itz.lirdev.actions.impl.TitleAction;
import itz.lirdev.actions.impl.WaitAction;
import itz.lirdev.tools.Logger;
import itz.lirdev.tools.SchedulerUtil;

public class ActionHandler {

    private final JavaPlugin plugin;

    public ActionHandler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public List<Action> parseActions(List<String> actionLines) {
        List<Action> actions = new ArrayList<>();
        if (actionLines == null) {
            return actions;
        }

        for (String line : actionLines) {
            if (line == null || line.trim().isEmpty()) {
                continue;
            }
            try {
                Action action = parseAction(line);
                if (action != null) {
                    actions.add(action);
                } else {
                    Logger.error("Unknown action tag: " + line.trim());
                }
            } catch (Exception e) {
                Logger.error("Failed to parse action: " + line.trim() + " - " + e.getMessage());
            }
        }
        return actions;
    }

    private Action parseAction(String line) {
        ActionType type = ActionType.detect(line);
        if (type == null) {
            return null;
        }

        String content = type.extractContent(line);
        if (!content.isEmpty() && content.charAt(0) == ' ') {
            content = content.substring(1);
        }

        return switch (type) {
            case MESSAGE ->
                new MessageAction(content);
            case ACTIONBAR ->
                new ActionBarAction(content);
            case TITLE ->
                new TitleAction(content);
            case BOSSBAR ->
                new BossBarAction(content, plugin);
            case SOUND ->
                parseSoundAction(content);
            case WAIT ->
                new WaitAction(content);
            case EFFECT ->
                new EffectAction(content);
            case CONSOLE ->
                new ConsoleAction(content);
            case PLAYER ->
                new PlayerAction(content);
            case BUTTON ->
                new ButtonAction(ButtonAction.Mode.RUN_COMMAND, content);
            case BUTTON_URL ->
                new ButtonAction(ButtonAction.Mode.OPEN_URL, content);
            case BUTTON_SUGGEST ->
                new ButtonAction(ButtonAction.Mode.SUGGEST_COMMAND, content);
            case BUTTONS ->
                new ButtonsRowAction(content);
        };
    }

    private Action parseSoundAction(String content) {
        String[] parts = content.split("\\s+");
        float volume = safeParse(parts.length > 1 ? parts[1] : "", 1.0f);
        float pitch = safeParse(parts.length > 2 ? parts[2] : "", 1.0f);
        return new SoundAction(parts[0], volume, pitch);
    }

    private float safeParse(String value, float def) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public void executeActions(Player player, List<Action> actions) {
        executeFrom(player, actions, 0);
    }

    public void executeFrom(Player player, List<Action> actions, int startIndex) {
        int size = actions.size();

        for (int i = startIndex; i < size; i++) {
            if (!player.isOnline()) {
                return;
            }

            Action action = actions.get(i);

            if (action instanceof WaitAction wait) {
                final int nextIndex = i + 1;
                SchedulerUtil.runTaskLaterForPlayer(
                        plugin, player,
                        () -> executeFrom(player, actions, nextIndex),
                        wait.getTicks()
                );
                return;
            }

            action.execute(player);
        }
    }
}
