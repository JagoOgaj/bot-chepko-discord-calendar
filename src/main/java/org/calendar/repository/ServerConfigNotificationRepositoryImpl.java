package org.calendar.repository;

import org.calendar.database.Database;
import org.calendar.entity.ServerConfigNotificationEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ServerConfigNotificationRepositoryImpl implements ServerConfigNotificationRepository {

    public ServerConfigNotificationRepositoryImpl() {}

    @Override
    public Optional<ServerConfigNotificationEntity> findByServerId(Long serverId) {
        String sql = "SELECT * FROM server_config_notifications WHERE server_id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, serverId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<ServerConfigNotificationEntity> findById(Long serverId) {
        return this.findByServerId(serverId);
    }

    @Override
    public List<ServerConfigNotificationEntity> findAll() {
        List<ServerConfigNotificationEntity> list = new ArrayList<>();
        String sql = "SELECT * FROM server_config_notifications";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void save(ServerConfigNotificationEntity entity) {
        String sql =
                "INSERT INTO server_config_notifications (server_id, channel_id, reminder_minutes, enabled) VALUES (?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt =
                        conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, entity.serverId());
            stmt.setLong(2, entity.channelId());
            stmt.setInt(3, entity.reminderMinutes());
            stmt.setBoolean(4, entity.enabled());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(ServerConfigNotificationEntity entity) {
        String sql =
                "UPDATE server_config_notifications SET channel_id = ?, reminder_minutes = ?, enabled = ? WHERE server_id = ?";

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.NO_GENERATED_KEYS)) {
            stmt.setLong(1, entity.channelId());
            stmt.setInt(2, entity.reminderMinutes());
            stmt.setBoolean(3, entity.enabled());
            stmt.setLong(4, entity.serverId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Long serverId) {
        String sql = "DELETE FROM server_config_notifications WHERE server_id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.NO_GENERATED_KEYS)) {
            stmt.setLong(1, serverId);

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<ServerConfigNotificationEntity> findAllEnabled() {
        List<ServerConfigNotificationEntity> list = new ArrayList<>();
        String sql = "SELECT * FROM server_config_notifications WHERE enabled = TRUE";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public ServerConfigNotificationEntity mapRow(ResultSet rs) throws SQLException {
        return new ServerConfigNotificationEntity(
                rs.getLong("server_id"),
                rs.getLong("channel_id"),
                rs.getInt("reminder_minutes"),
                rs.getBoolean("enabled"));
    }
}
