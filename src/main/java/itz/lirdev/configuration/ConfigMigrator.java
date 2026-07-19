package itz.lirdev.configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import itz.lirdev.LirBroadcast;
import itz.lirdev.tools.FileLoader;
import itz.lirdev.tools.Logger;

public class ConfigMigrator {

    public static final int CURRENT_VERSION = 1;

    private static final Pattern CONFIG_VERSION_LINE = Pattern.compile("^config-version:\\s*\\d+\\s*$");

    private final File userFile;

    public ConfigMigrator(File userFile) {
        this.userFile = userFile;
    }

    public boolean migrate(FileConfiguration userConfig) {
        try {
            return doMigrate(userConfig);
        } catch (Exception e) {
            Logger.warn("Config migration failed unexpectedly, keeping config as-is: " + e.getMessage());
            return false;
        }
    }

    private boolean doMigrate(FileConfiguration userConfig) {
        int userVersion = userConfig.getInt("config-version", 0);

        if (userVersion >= CURRENT_VERSION) {
            return false;
        }

        Logger.warn("Config is outdated (version " + userVersion + " → " + CURRENT_VERSION + "). Migrating...");

        FileConfiguration defaultConfig = loadDefaultConfig();
        if (defaultConfig == null) {
            Logger.warn("Could not load bundled default config.yml from plugin jar, skipping migration");
            return false;
        }

        boolean changed;
        try {
            changed = migratePreservingComments(userConfig, defaultConfig);
        } catch (Exception e) {
            Logger.warn("Comment-preserving migration failed (" + e.getMessage()
                    + "), falling back to plain migration - existing comments in config.yml will be lost");
            changed = migrateLegacy(userConfig, defaultConfig, userVersion);
        }

        return changed;
    }

    private boolean migratePreservingComments(FileConfiguration userConfig, FileConfiguration defaultConfig)
            throws IOException {

        if (!userFile.exists()) {
            return false;
        }

        List<String> userLines = Files.readAllLines(userFile.toPath(), StandardCharsets.UTF_8);
        List<String> defaultLines = readResourceLines("config.yml");
        if (defaultLines == null) {
            return false;
        }

        YamlCommentMerger.MergeResult result = YamlCommentMerger.merge(
                userLines, defaultLines, userConfig, defaultConfig, CURRENT_VERSION);

        if (!result.changed()) {
            return false;
        }

        for (String key : result.addedKeys()) {
            Logger.info("  + Added missing key: " + key);
        }

        backupOriginal();
        Files.write(userFile.toPath(), result.lines(), StandardCharsets.UTF_8);
        FileLoader.invalidate(userFile.getName());
        return true;
    }

    private static List<String> readResourceLines(String name) {
        try (InputStream stream = LirBroadcast.getInstance().getResource(name)) {
            if (stream == null) {
                return null;
            }
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return new java.io.BufferedReader(reader).lines().toList();
            }
        } catch (IOException e) {
            Logger.warn("Failed to read bundled default config.yml: " + e.getMessage());
            return null;
        }
    }

    private boolean migrateLegacy(FileConfiguration userConfig, FileConfiguration defaultConfig, int userVersion) {
        boolean changed = false;

        for (String key : defaultConfig.getKeys(true)) {
            if (!userConfig.contains(key)) {
                userConfig.set(key, defaultConfig.get(key));
                Logger.info("  + Added missing key: " + key);
                changed = true;
            }
        }

        userConfig.set("config-version", CURRENT_VERSION);
        changed = true;

        if (changed) {
            backupOriginal();
            if (!save(userConfig)) {
                userConfig.set("config-version", userVersion);
                return false;
            }
            FileLoader.invalidate(userFile.getName());
        }

        return changed;
    }

    private FileConfiguration loadDefaultConfig() {
        try (InputStream stream = LirBroadcast.getInstance().getResource("config.yml")) {
            if (stream == null) {
                return null;
            }
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return YamlConfiguration.loadConfiguration(reader);
            }
        } catch (IOException e) {
            Logger.warn("Failed to read bundled default config.yml: " + e.getMessage());
            return null;
        }
    }

    private void backupOriginal() {
        if (!userFile.exists()) {
            return;
        }
        try {
            File backup = new File(userFile.getParentFile(), "config-backup.yml");
            Files.copy(userFile.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Logger.info("Backed up old config to " + backup.getName());
        } catch (IOException e) {
            Logger.warn("Failed to create config backup before migration: " + e.getMessage());
        }
    }

    private boolean save(FileConfiguration config) {
        try {
            config.save(userFile);
            return true;
        } catch (IOException e) {
            Logger.warn("Failed to save migrated config: " + e.getMessage());
            return false;
        }
    }

    static final class YamlCommentMerger {

        private YamlCommentMerger() {
        }

        record MergeResult(List<String> lines, boolean changed, List<String> addedKeys) {
        }

        static MergeResult merge(List<String> userLinesIn, List<String> defaultLines,
                FileConfiguration userConfig, FileConfiguration defaultConfig, int newVersion) {

            List<String> lines = new ArrayList<>(userLinesIn);
            List<Node> defaultTree = parse(defaultLines, 0, defaultLines.size(), 0);
            List<String> addedKeys = new ArrayList<>();

            List<String> allDefaultKeys = new ArrayList<>(defaultConfig.getKeys(true));
            allDefaultKeys.sort(Comparator.comparingInt(YamlCommentMerger::depth));

            Set<String> insertedWholeSections = new HashSet<>();

            for (String key : allDefaultKeys) {
                if (key.equals("config-version")) {
                    continue;
                }
                if (userConfig.contains(key)) {
                    continue;
                }
                if (coveredByAncestor(key, insertedWholeSections)) {
                    continue;
                }

                String[] path = key.split("\\.");
                Node defNode = navigate(defaultTree, path, 0);
                if (defNode == null) {
                    continue;
                }

                List<Node> userTree = parse(lines, 0, lines.size(), 0);
                String[] parentPath = Arrays.copyOf(path, path.length - 1);
                Node parentNode = parentPath.length == 0 ? null : navigate(userTree, parentPath, 0);

                List<String> block = new ArrayList<>(defaultLines.subList(defNode.blockStart, defNode.blockEnd));

                int insertAt;
                if (parentNode != null) {
                    int expectedIndent = childIndentOf(parentNode);
                    block = reindent(block, defNode.indent, expectedIndent);
                    insertAt = parentNode.blockEnd;
                } else {
                    insertAt = findTopLevelInsertPoint(lines);
                }

                List<String> toInsert = new ArrayList<>();
                if (insertAt > 0 && insertAt <= lines.size() && !lines.get(insertAt - 1).isBlank()) {
                    toInsert.add("");
                }
                toInsert.addAll(block);
                if (insertAt < lines.size() && !lines.get(insertAt).isBlank()) {
                    toInsert.add("");
                }

                lines.addAll(insertAt, toInsert);
                addedKeys.add(key);

                if (parentPath.length == 0 || !userConfig.contains(String.join(".", parentPath))) {
                    insertedWholeSections.add(key);
                }
            }

            boolean versionChanged = updateConfigVersion(lines, newVersion);
            boolean changed = !addedKeys.isEmpty() || versionChanged;

            return new MergeResult(lines, changed, addedKeys);
        }

        private static boolean coveredByAncestor(String key, Set<String> insertedWholeSections) {
            for (String ancestor : insertedWholeSections) {
                if (key.equals(ancestor) || key.startsWith(ancestor + ".")) {
                    return true;
                }
            }
            return false;
        }

        private static int depth(String key) {
            int count = 0;
            for (int i = 0; i < key.length(); i++) {
                if (key.charAt(i) == '.') {
                    count++;
                }
            }
            return count;
        }

        private static int childIndentOf(Node parent) {
            if (!parent.children.isEmpty()) {
                return parent.children.get(0).indent;
            }
            return parent.indent + 2;
        }

        private static List<String> reindent(List<String> block, int fromIndent, int toIndent) {
            if (fromIndent == toIndent) {
                return block;
            }
            int delta = toIndent - fromIndent;
            List<String> out = new ArrayList<>(block.size());
            for (String line : block) {
                if (line.isBlank()) {
                    out.add(line);
                    continue;
                }
                int cur = indentOf(line);
                int updated = Math.max(0, cur + delta);
                out.add(" ".repeat(updated) + line.stripLeading());
            }
            return out;
        }

        private static int findTopLevelInsertPoint(List<String> lines) {
            for (int i = 0; i < lines.size(); i++) {
                String trimmed = lines.get(i).strip();
                if (indentOf(lines.get(i)) == 0 && trimmed.startsWith("config-version")) {
                    int start = i;
                    int back = i - 1;
                    while (back >= 0) {
                        String prev = lines.get(back);
                        if (prev.strip().startsWith("#") && indentOf(prev) == 0) {
                            start = back;
                            back--;
                        } else {
                            break;
                        }
                    }
                    return start;
                }
            }
            return lines.size();
        }

        private static boolean updateConfigVersion(List<String> lines, int newVersion) {
            for (int i = 0; i < lines.size(); i++) {
                Matcher m = CONFIG_VERSION_LINE.matcher(lines.get(i).strip());
                if (indentOf(lines.get(i)) == 0 && m.matches()) {
                    String replaced = "config-version: " + newVersion;
                    if (!lines.get(i).equals(replaced)) {
                        lines.set(i, replaced);
                        return true;
                    }
                    return false;
                }
            }
            if (!lines.isEmpty() && !lines.get(lines.size() - 1).isBlank()) {
                lines.add("");
            }
            lines.add("# Do not change this value. Used to automatically migrate your config on plugin updates.");
            lines.add("config-version: " + newVersion);
            return true;
        }

        private static Node navigate(List<Node> nodes, String[] path, int idx) {
            for (Node n : nodes) {
                if (n.key.equals(path[idx])) {
                    if (idx == path.length - 1) {
                        return n;
                    }
                    return navigate(n.children, path, idx + 1);
                }
            }
            return null;
        }

        private static List<Node> parse(List<String> lines, int start, int end, int indent) {
            List<Node> result = new ArrayList<>();
            int i = start;
            int commentStart = -1;

            while (i < end) {
                String raw = lines.get(i);
                String trimmed = raw.strip();

                if (trimmed.isEmpty()) {
                    commentStart = -1;
                    i++;
                    continue;
                }

                int lineIndent = indentOf(raw);
                if (lineIndent < indent) {
                    break;
                }
                if (lineIndent > indent) {
                    i++;
                    continue;
                }

                if (trimmed.charAt(0) == '#') {
                    if (commentStart == -1) {
                        commentStart = i;
                    }
                    i++;
                    continue;
                }

                if (trimmed.charAt(0) == '-') {
                    commentStart = -1;
                    i++;
                    continue;
                }

                int colon = findTopColon(trimmed);
                if (colon == -1) {
                    commentStart = -1;
                    i++;
                    continue;
                }

                String key = stripQuotes(trimmed.substring(0, colon).trim());
                String afterColon = trimmed.substring(colon + 1).trim();

                int blockStart = commentStart != -1 ? commentStart : i;
                int keyLine = i;
                commentStart = -1;

                int j = keyLine + 1;
                int blockEnd = keyLine + 1;
                while (j < end) {
                    String l2 = lines.get(j);
                    if (l2.strip().isEmpty()) {
                        j++;
                        continue;
                    }
                    int ind2 = indentOf(l2);
                    if (ind2 > indent) {
                        blockEnd = j + 1;
                        j++;
                    } else {
                        break;
                    }
                }

                List<Node> children = List.of();
                if (afterColon.isEmpty() && blockEnd > keyLine + 1) {
                    int childIndent = -1;
                    for (int k = keyLine + 1; k < blockEnd; k++) {
                        if (!lines.get(k).strip().isEmpty()) {
                            childIndent = indentOf(lines.get(k));
                            break;
                        }
                    }
                    if (childIndent > indent) {
                        children = parse(lines, keyLine + 1, blockEnd, childIndent);
                    }
                }

                result.add(new Node(key, indent, blockStart, blockEnd, children));
                i = blockEnd;
            }

            return result;
        }

        private static int indentOf(String line) {
            int count = 0;
            while (count < line.length() && line.charAt(count) == ' ') {
                count++;
            }
            return count;
        }

        private static int findTopColon(String trimmed) {
            boolean inSingle = false;
            boolean inDouble = false;
            for (int i = 0; i < trimmed.length(); i++) {
                char c = trimmed.charAt(i);
                if (c == '\'' && !inDouble) {
                    inSingle = !inSingle;
                } else if (c == '"' && !inSingle) {
                    inDouble = !inDouble;
                } else if (c == ':' && !inSingle && !inDouble) {
                    if (i + 1 == trimmed.length() || trimmed.charAt(i + 1) == ' ') {
                        return i;
                    }
                }
            }
            return -1;
        }

        private static String stripQuotes(String s) {
            if (s.length() >= 2) {
                char first = s.charAt(0);
                char last = s.charAt(s.length() - 1);
                if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                    return s.substring(1, s.length() - 1);
                }
            }
            return s;
        }

        private static final class Node {
            final String key;
            final int indent;
            final int blockStart;
            final int blockEnd;
            final List<Node> children;

            Node(String key, int indent, int blockStart, int blockEnd, List<Node> children) {
                this.key = key;
                this.indent = indent;
                this.blockStart = blockStart;
                this.blockEnd = blockEnd;
                this.children = children;
            }
        }
    }
}