package itz.lirdev.managers;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import itz.lirdev.tools.Logger;

public class DatabaseManager {

    private Connection connection;
    private final ConcurrentHashMap<UUID, Boolean> notificationsCache = new ConcurrentHashMap<>();

    private final ExecutorService dbExecutor
            = Executors.newSingleThreadExecutor(r -> new Thread(r, "LirBroadcast-SQLite"));

    public DatabaseManager(File dataFolder) {
        try {
            Class.forName("org.sqlite.JDBC");

            File dbFile = new File(dataFolder, "disabled_players.db");
            connection = DriverManager.getConnection(
                    "jdbc:sqlite:" + dbFile.getAbsolutePath()
            );

            try (Statement s = connection.createStatement()) {
                s.execute("PRAGMA journal_mode = DELETE");
                s.execute("PRAGMA synchronous = NORMAL");
                s.execute("PRAGMA cache_size = 2000");
                s.execute("PRAGMA temp_store = MEMORY");
            }

            createTables();

            loadAllIntoCache();

            Logger.info("Database initialized successfully");

        } catch (Exception e) {
            Logger.error("Failed to initialize database: " + e.getMessage());
        }
    }

    private void createTables() throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS players (
                    uuid VARCHAR(36) PRIMARY KEY,
                    notifications_disabled INTEGER DEFAULT 0
                )
                """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    private void loadAllIntoCache() {
        if (connection == null) {
            return;
        }

        try {
            dbExecutor.submit(() -> {
                try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(
                        "SELECT uuid, notifications_disabled FROM players")) {
                    while (rs.next()) {
                        UUID uuid = UUID.fromString(rs.getString("uuid"));
                        boolean isDis = rs.getInt("notifications_disabled") == 1;
                        notificationsCache.put(uuid, isDis);
                    }
                } catch (SQLException e) {
                    Logger.error("Failed to load players: " + e.getMessage());
                }
            }).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            Logger.error("Failed to load players from DB: " + e.getMessage());
        }
    }

    public void disableNotifications(UUID uuid) {
        notificationsCache.put(uuid, true);
        saveAsync(uuid, true);
    }

    public void enableNotifications(UUID uuid) {
        notificationsCache.put(uuid, false);
        saveAsync(uuid, false);
    }

    public boolean isNotificationDisabled(UUID uuid) {
        return notificationsCache.getOrDefault(uuid, false);
    }

    public Set<UUID> getDisabledSet() {
        Set<UUID> disabled = new HashSet<>();
        notificationsCache.forEach((uuid, isDis) -> {
            if (isDis) {
                disabled.add(uuid);
            }
        });
        return disabled;
    }

    @Deprecated
    public Set<UUID> loadAllDisabledPlayers() {
        return getDisabledSet();
    }

    private void saveAsync(UUID uuid, boolean disabled) {
        if (connection == null) {
            return;
        }

        dbExecutor.execute(() -> {
            String sql = "INSERT OR REPLACE INTO players (uuid, notifications_disabled) VALUES (?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, uuid.toString());
                ps.setInt(2, disabled ? 1 : 0);
                ps.executeUpdate();
            } catch (SQLException e) {
                Logger.error("Failed to save notification status: " + e.getMessage());
            }
        });
    }

    public void close() {
        try {
            dbExecutor.shutdown();
            if (!dbExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                dbExecutor.shutdownNow();
            }
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            Logger.info("Database connection closed");
        } catch (Exception e) {
            Logger.error("Failed to close database: " + e.getMessage());
        }
    }
}
