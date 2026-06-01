package itz.lirdev.actions;

public enum ActionType {

    MESSAGE("[message]"),
    ACTIONBAR("[actionbar]"),
    TITLE("[title]"),
    BOSSBAR("[bossbar]"),
    SOUND("[sound]"),
    WAIT("[wait]"),
    EFFECT("[effect]"),
    CONSOLE("[console]"),
    PLAYER("[player]"),
    BUTTON("[button]"),
    BUTTON_URL("[button-url]"),
    BUTTON_SUGGEST("[button-suggest]"),
    BUTTONS("[buttons]");

    private final String tag;

    ActionType(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public static ActionType detect(String line) {
        if (line == null) {
            return null;
        }
        for (ActionType type : values()) {
            if (line.startsWith(type.tag)) {
                return type;
            }
        }
        return null;
    }

    public String extractContent(String line) {
        String raw = line.substring(tag.length());
        return switch (this) {
            case MESSAGE, ACTIONBAR, BUTTON, BUTTON_URL, BUTTON_SUGGEST, BUTTONS ->
                raw;
            default ->
                raw.trim();
        };
    }
}
