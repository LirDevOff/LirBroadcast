package itz.lirdev.actions.impl;

import org.bukkit.entity.Player;

import itz.lirdev.actions.Action;
import itz.lirdev.tools.ColorParser;

public class MessageAction implements Action {

    private final String message;

    public MessageAction(String message) {
        this.message = message;
    }

    @Override
    public void execute(Player player) {
        String colorized = ColorParser.colorize(message);
        String parsed = ColorParser.setPapi(colorized, player);
        player.sendMessage(parsed);
    }

}
