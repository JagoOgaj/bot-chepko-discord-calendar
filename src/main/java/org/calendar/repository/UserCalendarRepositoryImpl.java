package org.calendar.repository;

import org.calendar.database.Database;
import org.calendar.entity.UserCalendarEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserCalendarRepositoryImpl implements UserCalendarRepository {

    public UserCalendarRepositoryImpl() {}

    @Override
    public Optional<UserCalendarEntity> findById(Integer integer) {
        throw new UnsupportedOperationException("Use findByUserAndCalendar instead.");
    }

    @Override
    public List<UserCalendarEntity> findAll() {
        List<UserCalendarEntity> list = new ArrayList<>();
        String sql = "SELECT * FROM user_calendars";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void save(UserCalendarEntity entity) {
        String sql = "INSERT INTO user_calendars (user_id, calendar_id) VALUES (?, ?)";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt =
                        conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, entity.userId());
            stmt.setInt(2, entity.calendarId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(UserCalendarEntity entity) {
        throw new UnsupportedOperationException("Use deleteByUserAndCalendar instead.");
    }

    @Override
    public void delete(Integer integer) {
        throw new UnsupportedOperationException("Use deleteByUserAndCalendar instead.");
    }

    @Override
    public UserCalendarEntity mapRow(ResultSet rs) throws SQLException {
        return new UserCalendarEntity(rs.getInt("user_id"), rs.getInt("calendar_id"));
    }

    @Override
    public Optional<UserCalendarEntity> findByUserAndCalendar(int userId, int calendarId) {
        String sql = "SELECT * FROM user_calendars WHERE user_id = ? AND calendar_id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, calendarId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<UserCalendarEntity> findByUserId(int userId) {
        String sql = "SELECT * FROM user_calendars WHERE user_id = ?";
        List<UserCalendarEntity> list = new ArrayList<>();
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<UserCalendarEntity> findByCalendarId(int calendarId) {
        String sql = "SELECT * FROM user_calendars WHERE calendar_id = ?";
        List<UserCalendarEntity> list = new ArrayList<>();
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, calendarId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void deleteByUserAndCalendar(int userId, int calendarId) {
        String sql = "DELETE FROM user_calendars WHERE user_id = ? AND calendar_id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.NO_GENERATED_KEYS)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, calendarId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
