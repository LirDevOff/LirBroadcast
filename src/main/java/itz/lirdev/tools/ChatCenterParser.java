package itz.lirdev.tools;

public class ChatCenterParser {

    private ChatCenterParser() {
    }

    public static String center(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        int spaces = Math.max(0, (320 - getWidth(stripForMeasure(text))) / 2 / 4);
        return " ".repeat(spaces) + text;
    }

    public static String right(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        int spaces = Math.max(0, (320 - getWidth(stripForMeasure(text))) / 4);
        return " ".repeat(spaces) + text;
    }

    private static String stripForMeasure(String text) {
        if (text == null) {
            return "";
        }

        int len = text.length();
        StringBuilder sb = new StringBuilder(len);

        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);

            if (c == '&' && i + 7 < len && text.charAt(i + 1) == '#') {
                boolean isHex = true;
                for (int j = i + 2; j < i + 8; j++) {
                    char h = text.charAt(j);
                    if (!((h >= '0' && h <= '9') || (h >= 'a' && h <= 'f') || (h >= 'A' && h <= 'F'))) {
                        isHex = false;
                        break;
                    }
                }
                if (isHex) {
                    i += 7;
                    continue;
                }
            }

            if (c == '§' && i + 1 < len) {
                char code = Character.toLowerCase(text.charAt(i + 1));
                if (code == 'x' && i + 13 < len) {
                    i += 13;
                    continue;
                }
                i += 1;
                continue;
            }

            if (c == '<') {
                int close = text.indexOf('>', i + 1);
                if (close != -1) {
                    i = close;
                    continue;
                }
            }

            sb.append(c);
        }

        return sb.toString();
    }

    public static int getWidth(String text) {
        int width = 0;
        boolean bold = false;
        int len = text.length();

        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);

            if (c == '§' && i + 1 < len) {
                char code = Character.toLowerCase(text.charAt(++i));
                if (code == 'l') {
                    bold = true;
                } else if (code == 'r'
                        || (code >= '0' && code <= '9')
                        || (code >= 'a' && code <= 'f')) {
                    bold = false;
                }
                continue;
            }

            int cw = getCharWidth(c);
            width += bold ? cw + 2 : cw + 1;
        }

        return width;
    }

    private static int getCharWidth(char c) {
        return switch (c) {
            case ' ' ->
                3;
            case 'i', '!', ',', '.', ':', ';', '|', '\'' ->
                1;
            case 'l', '`' ->
                2;
            case 'I', '"', '(', ')', '*', '[', ']' ->
                3;
            case 'f', 't', '{', '}' ->
                4;
            case 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' ->
                6;
            default ->
                5;
        };
    }
}
