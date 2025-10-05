package com.example.annotationextractor.adapters.persistence.jdbc;

import com.example.annotationextractor.database.DatabaseConfig;
import com.example.annotationextractor.domain.model.TestClass;
import com.example.annotationextractor.domain.port.TestClassPort;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTestClassAdapter implements TestClassPort {

    @Override
    public Optional<TestClass> findById(Long id) {
        String sql = "SELECT * FROM test_classes WHERE id = ?";
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
    public List<TestClass> findByRepositoryIdAndScanSessionId(Long repositoryId, Long scanSessionId) {
        String sql = "SELECT * FROM test_classes WHERE repository_id = ? and scan_session_id = ? ORDER BY id";
        List<TestClass> result = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, repositoryId);
            stmt.setLong(2, scanSessionId);
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
    public Optional<TestClass> findByRepositoryIdAndScanSessionIdAndFilePath(Long repositoryId, Long scanSessionId, String filePath) {
        String sql = "SELECT * FROM test_classes WHERE repository_id = ? AND scan_session_id = ? AND file_path = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, repositoryId);
            stmt.setLong(2, scanSessionId);
            stmt.setString(3, filePath);
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
    public List<TestClass> findAllByScanSessionId(Long scanSessionId) {
        String sql = "SELECT * FROM test_classes WHERE scan_session_id = ? ORDER BY id";
        List<TestClass> result = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, scanSessionId);
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
    public long countAllByScanSessionId(Long scanSessionId) {
        String sql = "SELECT COUNT(*) FROM test_classes WHERE scan_session_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, scanSessionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private TestClass mapRow(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        Long repositoryId = rs.getLong("repository_id");
        String className = rs.getString("class_name");
        String packageName = rs.getString("package_name");
        String filePath = rs.getString("file_path");
        Long fileSizeBytes = (Long) rs.getObject("file_size_bytes");
        int totalTestMethods = rs.getInt("total_test_methods");
        int annotatedTestMethods = rs.getInt("annotated_test_methods");
        double coverageRate = rs.getDouble("coverage_rate");
        Timestamp firstSeen = rs.getTimestamp("first_seen_date");
        Timestamp lastModified = rs.getTimestamp("last_modified_date");
        Long scanSessionId = (Long) rs.getObject("scan_session_id");

        return new TestClass(
            id,
            repositoryId,
            className,
            packageName,
            filePath,
            fileSizeBytes,
            totalTestMethods,
            annotatedTestMethods,
            coverageRate,
            firstSeen != null ? firstSeen.toInstant() : null,
            lastModified != null ? lastModified.toInstant() : null,
            scanSessionId
        );
    }
}


