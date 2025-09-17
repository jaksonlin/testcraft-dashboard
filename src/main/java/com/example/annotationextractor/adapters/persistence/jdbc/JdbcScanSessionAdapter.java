package com.example.annotationextractor.adapters.persistence.jdbc;

import com.example.annotationextractor.database.DatabaseConfig;
import com.example.annotationextractor.domain.model.ScanSession;
import com.example.annotationextractor.domain.port.ScanSessionPort;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcScanSessionAdapter implements ScanSessionPort {

    @Override
    public Optional<ScanSession> findById(Long id) {
        String sql = "SELECT * FROM scan_sessions WHERE id = ?";
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
    public List<ScanSession> findAll() {
        String sql = "SELECT * FROM scan_sessions ORDER BY id DESC";
        List<ScanSession> result = new ArrayList<>();
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
    public List<ScanSession> findRecent(int limit) {
        String sql = "SELECT * FROM scan_sessions ORDER BY scan_date DESC LIMIT ?";
        List<ScanSession> result = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM scan_sessions";
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

    private ScanSession mapRow(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        Timestamp scanDate = rs.getTimestamp("scan_date");
        String scanDirectory = rs.getString("scan_directory");
        int totalRepositories = rs.getInt("total_repositories");
        int totalTestClasses = rs.getInt("total_test_classes");
        int totalTestMethods = rs.getInt("total_test_methods");
        int totalAnnotatedMethods = rs.getInt("total_annotated_methods");
        long scanDurationMs = rs.getLong("scan_duration_ms");
        String scanStatus = rs.getString("scan_status");
        String errorLog = rs.getString("error_log");
        String metadata = rs.getString("metadata");
        return new ScanSession(
            id,
            scanDate != null ? scanDate.toInstant() : null,
            scanDirectory,
            totalRepositories,
            totalTestClasses,
            totalTestMethods,
            totalAnnotatedMethods,
            scanDurationMs,
            scanStatus,
            errorLog,
            metadata
        );
    }
}


