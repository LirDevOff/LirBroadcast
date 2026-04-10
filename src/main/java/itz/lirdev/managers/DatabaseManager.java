package itz.lirdev.managers;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import itz.lirdev.tools.Logger;

public class DatabaseManager {

    private Connection connection;
    private final Map<UUID, Boolean> notificationsCache;

    public DatabaseManager(File dataFolder) {
        this.notificationsCache = new HashMap<>();
        initialize(dataFolder);
    }

    public void initialize(File dataFolder) {
        try {
            Class.forName("org.sqlite.JDBC");
            File dbFile = new File(dataFolder, "lirbroadcast.db");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            createTables();
            Logger.info("Database initialized successfully");
        } catch (Exception e) {
            Logger.error("Failed to initialize database: " + e.getMessage());
        }
    }

    private void createTables() throws SQLException {
        String playersTable = "CREATE TABLE IF NOT EXISTS players ("
                + "uuid VARCHAR(36) PRIMARY KEY,"
                + "notifications_disabled INTEGER DEFAULT 0"
                + ")";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(playersTable);
        }
    }

    public void disableNotifications(UUID playerUUID) {
        notificationsCache.put(playerUUID, true);
        saveToDatabase(playerUUID, true);
    }

    public void enableNotifications(UUID playerUUID) {
        notificationsCache.put(playerUUID, false);
        saveToDatabase(playerUUID, false);
    }

    public boolean isNotificationDisabled(UUID playerUUID) {
        if (notificationsCache.containsKey(playerUUID)) {
            return notificationsCache.get(playerUUID);
        }

        String sql = "SELECT notifications_disabled FROM players WHERE uuid = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID.toString());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                boolean disabled = rs.getInt("notifications_disabled") == 1;
                notificationsCache.put(playerUUID, disabled);
                return disabled;
            } else {
                notificationsCache.put(playerUUID, false);
                return false;
            }
        } catch (SQLException e) {
            Logger.error("Failed to check notification status: " + e.getMessage());
            return false;
        }
    }

    public void loadAllDisabledPlayers() {
        notificationsCache.clear();

        String sql = "SELECT uuid FROM players WHERE notifications_disabled = 1";

        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                notificationsCache.put(uuid, true);
            }
        } catch (SQLException e) {
            Logger.error("Failed to load disabled players: " + e.getMessage());
        }
    }

    private void saveToDatabase(UUID playerUUID, boolean disabled) {
        String sql = "INSERT OR REPLACE INTO players (uuid, notifications_disabled) VALUES (?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUUID.toString());
            pstmt.setInt(2, disabled ? 1 : 0);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            Logger.error("Failed to save notification status: " + e.getMessage());
        }
    }

    public void invalidateCache(UUID playerUUID) {
        notificationsCache.remove(playerUUID);
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                Logger.info("Database connection closed");
            }
        } catch (SQLException e) {
            Logger.error("Failed to close database: " + e.getMessage());
        }
    }

}
