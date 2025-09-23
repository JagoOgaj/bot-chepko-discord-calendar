package org.calendar.repository;

import org.calendar.database.Database;
import org.calendar.entity.CalendarEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CalendarRepositoryImpl implements CalendarRepository {

    public CalendarRepositoryImpl() {}

    @Override
    public Optional<CalendarEntity> findById(Integer id) {
        String sql = "SELECT * FROM calendars WHERE id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<CalendarEntity> findAll() {
        List<CalendarEntity> list = new ArrayList<>();
        String sql = "SELECT * FROM calendars";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void save(CalendarEntity entity) {
        String sql =
                "INSERT INTO calendars (server_id, ics_url, name, last_updated) VALUES (?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt =
                        conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, entity.serverId());
            stmt.setString(2, entity.icsUrl());
            stmt.setString(3, entity.name());
            stmt.setTimestamp(
                    4,
                    entity.lastUpdated() != null ? Timestamp.valueOf(entity.lastUpdated()) : null);

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(CalendarEntity entity) {
        String sql = "UPDATE calendars SET ics_url = ?, name = ?, last_updated = ? WHERE id = ?";

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, entity.icsUrl());
            stmt.setString(2, entity.name());
            stmt.setTimestamp(
                    3,
                    entity.lastUpdated() != null ? Timestamp.valueOf(entity.lastUpdated()) : null);
            stmt.setInt(4, entity.calendarId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM calendars WHERE id = ?";

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public CalendarEntity mapRow(ResultSet rs) throws SQLException {
        return new CalendarEntity(
                rs.getInt("id"),
                rs.getLong("server_id"),
                rs.getString("ics_url"),
                rs.getString("name"),
                rs.getTimestamp("last_updated") != null
                        ? rs.getTimestamp("last_updated").toLocalDateTime()
                        : null);
    }

    @Override
    public Optional<CalendarEntity> findByServerId(Long serverId) {
        String sql = "SELECT * FROM calendars WHERE server_id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, serverId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
