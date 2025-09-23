package org.calendar.repository;

import org.apache.commons.lang3.NotImplementedException;
import org.calendar.database.Database;
import org.calendar.entity.ServerUpdateAlertEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ServerUpdateAlertRepositoryImpl implements ServerUpdateAlertRepository {

    public ServerUpdateAlertRepositoryImpl() {}

    @Override
    public Optional<ServerUpdateAlertEntity> findById(Long serverId) {
        String sql = "SELECT server_id FROM server_update_alerts WHERE server_id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, serverId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<ServerUpdateAlertEntity> findAll() {
        List<ServerUpdateAlertEntity> servers = new ArrayList<>();
        String sql = "SELECT server_id FROM server_update_alerts";

        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                servers.add(this.mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return servers;
    }

    @Override
    public void save(ServerUpdateAlertEntity entity) {
        String sql =
                "INSERT INTO server_update_alerts (server_id) VALUES (?) ON CONFLICT DO NOTHING";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, entity.serverId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(ServerUpdateAlertEntity entity) {
        throw new NotImplementedException("Update n'existe pas");
    }

    @Override
    public void delete(Long serverId) {
        String sql = "DELETE FROM server_update_alerts WHERE server_id = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, serverId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ServerUpdateAlertEntity mapRow(ResultSet rs) throws SQLException {
        return new ServerUpdateAlertEntity(rs.getLong("server_id"));
    }
}
