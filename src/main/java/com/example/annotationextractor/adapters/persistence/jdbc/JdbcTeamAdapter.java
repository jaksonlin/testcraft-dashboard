package com.example.annotationextractor.adapters.persistence.jdbc;

import com.example.annotationextractor.database.DatabaseConfig;
import com.example.annotationextractor.domain.model.Team;
import com.example.annotationextractor.domain.port.TeamPort;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTeamAdapter implements TeamPort {

    @Override
    public Optional<Team> findById(Long id) {
        String sql = "SELECT * FROM teams WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Team> findByTeamCode(String teamCode) {
        String sql = "SELECT * FROM teams WHERE team_code = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, teamCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Team> findAll() {
        String sql = "SELECT * FROM teams ORDER BY team_name";
        List<Team> result = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM teams";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Team mapRow(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String teamName = rs.getString("team_name");
        String teamCode = rs.getString("team_code");
        String department = rs.getString("department");
        Timestamp created = rs.getTimestamp("created_date");
        Timestamp updated = rs.getTimestamp("last_updated_date");
        return new Team(
            id,
            teamName,
            teamCode,
            department,
            created != null ? created.toInstant() : null,
            updated != null ? updated.toInstant() : null
        );
    }
}


