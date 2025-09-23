package org.calendar.repository;

import org.calendar.database.Database;
import org.calendar.entity.EventNotificationEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EventNotificationRepositoryImpl implements EventNotificationRepository {

    public EventNotificationRepositoryImpl() {}

    @Override
    public boolean isEventNotified(int eventId, long serverId) {
        String sql = "SELECT 1 FROM event_notifications WHERE event_id = ? AND server_id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, eventId);
            stmt.setLong(2, serverId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteByEventAndServer(int eventId, Long serverId) {
        String sql = "DELETE FROM event_notifications WHERE event_id = ? AND server_id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.NO_GENERATED_KEYS)) {
            stmt.setInt(1, eventId);
            stmt.setLong(2, serverId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<EventNotificationEntity> findById(Integer integer) {
        throw new UnsupportedOperationException("Utiliser findByEventAndServer");
    }

    @Override
    public List<EventNotificationEntity> findAll() {
        List<EventNotificationEntity> list = new ArrayList<>();
        String sql = "SELECT * FROM event_notifications";
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
    public void save(EventNotificationEntity entity) {
        String sql =
                "INSERT INTO event_notifications (event_id, server_id, notified_at) VALUES (?, ?, ?)";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt =
                        conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, entity.eventId());
            stmt.setLong(2, entity.serverId());
            stmt.setTimestamp(3, Timestamp.valueOf(entity.notifiedAt()));

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(EventNotificationEntity entity) {
        String sql =
                "UPDATE event_notifications SET notified_at = ? WHERE event_id = ? AND server_id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.NO_GENERATED_KEYS)) {
            stmt.setTimestamp(1, Timestamp.valueOf(entity.notifiedAt()));
            stmt.setInt(2, entity.eventId());
            stmt.setLong(3, entity.serverId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Integer integer) {
        throw new UnsupportedOperationException("Use deleteByEventAndServer instead.");
    }

    @Override
    public EventNotificationEntity mapRow(ResultSet rs) throws SQLException {
        return new EventNotificationEntity(
                rs.getInt("event_id"),
                rs.getLong("server_id"),
                rs.getTimestamp("notified_at").toLocalDateTime());
    }
}
