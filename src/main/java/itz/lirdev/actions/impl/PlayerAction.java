package itz.lirdev.actions.impl;

import org.bukkit.entity.Player;

import itz.lirdev.actions.Action;
import itz.lirdev.tools.ColorParser;

public class PlayerAction implements Action {

    private final String command;
    private final boolean hasPapi;

    public PlayerAction(String content) {
        this.command = content.trim();
        this.hasPapi = content.contains("%");
    }

    @Override
    public void execute(Player player) {
        if (!hasPapi) {
            player.performCommand(command);
            return;
        }

        String name = player.getName();
        String cmd = command
                .replace("%player%", name)
                .replace("%player_name%", name);
        cmd = ColorParser.setPapi(cmd, player);
        player.performCommand(cmd);
    }
}
