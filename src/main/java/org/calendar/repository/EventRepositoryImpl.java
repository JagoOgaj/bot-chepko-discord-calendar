package org.calendar.repository;

import org.calendar.database.Database;
import org.calendar.entity.EventEntity;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EventRepositoryImpl implements EventRepository {

    public EventRepositoryImpl() {}

    @Override
    public Optional<EventEntity> findById(Integer id) {
        String sql = "SELECT * FROM events WHERE id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<EventEntity> findAll() {
        List<EventEntity> events = new ArrayList<>();
        String sql = "SELECT * FROM events";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                events.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }

    @Override
    public void save(EventEntity entity) {
        String sql =
                "INSERT INTO events (calendar_id, uid, summary, description, location, start_time, end_time, created_at, updated_at) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt =
                        conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, entity.calendarId());
            stmt.setString(2, entity.uid());
            stmt.setString(3, entity.summary());
            stmt.setString(4, entity.description());
            stmt.setString(5, entity.location());
            stmt.setTimestamp(6, Timestamp.valueOf(entity.start_time()));
            stmt.setTimestamp(7, Timestamp.valueOf(entity.end_time()));

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(EventEntity entity) {
        String sql =
                "UPDATE events SET summary=?, description=?, location=?, start_time=?, end_time=?, updated_at=NOW() WHERE id=?";

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.NO_GENERATED_KEYS)) {
            stmt.setString(1, entity.summary());
            stmt.setString(2, entity.description());
            stmt.setString(3, entity.location());
            stmt.setTimestamp(4, Timestamp.valueOf(entity.start_time()));
            stmt.setTimestamp(5, Timestamp.valueOf(entity.end_time()));
            stmt.setInt(6, entity.eventId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM events WHERE id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.NO_GENERATED_KEYS)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public EventEntity mapRow(ResultSet rs) throws SQLException {
        return new EventEntity(
                rs.getInt("id"),
                rs.getInt("calendar_id"),
                rs.getString("uid"),
                rs.getString("summary"),
                rs.getString("description"),
                rs.getString("location"),
                rs.getTimestamp("start_time").toLocalDateTime(),
                rs.getTimestamp("end_time").toLocalDateTime(),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime());
    }


    @Override
    public List<EventEntity> findByCalendarId(int calendarId) {
        String sql = "SELECT * FROM events WHERE calendar_id = ? ORDER BY start_time";
        List<EventEntity> events = new ArrayList<>();
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, calendarId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                events.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }

    @Override
    public List<EventEntity> findUpcomingEvents(int calendarId, LocalDateTime fromDate) {
        String sql =
                "SELECT * FROM events WHERE calendar_id = ? AND start_time >= ? ORDER BY start_time";
        List<EventEntity> events = new ArrayList<>();
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, calendarId);
            stmt.setTimestamp(2, Timestamp.valueOf(fromDate));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                events.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }


    @Override
    public void deleteByCalendarId(int calendarId) {
        String sql = "DELETE FROM events WHERE calendar_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, calendarId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
