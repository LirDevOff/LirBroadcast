package itz.lirdev.tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeParser {

    private static final Pattern PATTERN
            = Pattern.compile("(\\d+(?:\\.\\d+)?)(mo|y|w|d|h|m|s)");

    private TimeParser() {
    }

    public static long parse(String s) {
        if (s == null || s.isBlank()) {
            return 0;
        }

        s = s.toLowerCase().trim();

        if (s.matches("\\d+(\\.\\d+)?")) {
            return Math.round(Double.parseDouble(s) * 1000);
        }

        Matcher matcher = PATTERN.matcher(s);
        long out = 0;

        while (matcher.find()) {
            double value = Double.parseDouble(matcher.group(1));
            String type = matcher.group(2);

            out += getResult(value, type);
        }

        return out;
    }

    public static long toSeconds(String s) {
        return parse(s) / 1000;
    }

    public static long toTicks(String s) {
        return parse(s) / 50;
    }

    private static long getResult(double x, String s) {
        return switch (s) {
            case "s" ->
                (long) (1000L * x);
            case "m" ->
                (long) (60_000L * x);
            case "h" ->
                (long) (3_600_000L * x);
            case "d" ->
                (long) (86_400_000L * x);
            case "w" ->
                (long) (604_800_000L * x);
            case "mo" ->
                (long) (2_629_746_000L * x);
            case "y" ->
                (long) (31_556_908_800L * x);
            default ->
                0;
        };
    }
}
