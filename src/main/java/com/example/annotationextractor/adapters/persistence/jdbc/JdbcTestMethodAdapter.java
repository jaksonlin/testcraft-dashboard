package com.example.annotationextractor.adapters.persistence.jdbc;

import com.example.annotationextractor.database.DatabaseConfig;
import com.example.annotationextractor.domain.model.TestMethod;
import com.example.annotationextractor.domain.port.TestMethodPort;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTestMethodAdapter implements TestMethodPort {

    @Override
    public Optional<TestMethod> findById(Long id) {
        String sql = "SELECT * FROM test_methods WHERE id = ?";
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
    public List<TestMethod> findByTestClassId(Long testClassId) {
        String sql = "SELECT * FROM test_methods WHERE test_class_id = ? ORDER BY id";
        List<TestMethod> result = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, testClassId);
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
    public List<TestMethod> findByScanSessionId(Long scanSessionId) {
        String sql = "SELECT * FROM test_methods WHERE scan_session_id = ? ORDER BY id";
        List<TestMethod> result = new ArrayList<>();
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
    public List<TestMethod> findAnnotatedByRepository(Long repositoryId) {
        String sql = "SELECT tm.* FROM test_methods tm JOIN test_classes tc ON tm.test_class_id = tc.id WHERE tc.repository_id = ? AND tm.has_annotation = TRUE ORDER BY tm.id";
        List<TestMethod> result = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, repositoryId);
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
        String sql = "SELECT COUNT(*) FROM test_methods";
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

    private TestMethod mapRow(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        Long testClassId = rs.getLong("test_class_id");
        String methodName = rs.getString("method_name");
        String methodSignature = rs.getString("method_signature");
        Integer lineNumber = (Integer) rs.getObject("line_number");
        boolean hasAnnotation = rs.getBoolean("has_annotation");
        String annotationData = rs.getString("annotation_data");
        String annotationTitle = rs.getString("annotation_title");
        String annotationAuthor = rs.getString("annotation_author");
        String annotationStatus = rs.getString("annotation_status");
        String annotationTargetClass = rs.getString("annotation_target_class");
        String annotationTargetMethod = rs.getString("annotation_target_method");
        String annotationDescription = rs.getString("annotation_description");
        String annotationTags = rs.getString("annotation_tags");
        String annotationTestPoints = rs.getString("annotation_test_points");
        String annotationRequirements = rs.getString("annotation_requirements");
        String annotationDefects = rs.getString("annotation_defects");
        String annotationTestcases = rs.getString("annotation_testcases");
        String annotationLastUpdateTime = rs.getString("annotation_last_update_time");
        String annotationLastUpdateAuthor = rs.getString("annotation_last_update_author");
        Timestamp firstSeen = rs.getTimestamp("first_seen_date");
        Timestamp lastModified = rs.getTimestamp("last_modified_date");
        Long scanSessionId = (Long) rs.getObject("scan_session_id");

        return new TestMethod(
            id,
            testClassId,
            methodName,
            methodSignature,
            lineNumber,
            hasAnnotation,
            annotationData,
            annotationTitle,
            annotationAuthor,
            annotationStatus,
            annotationTargetClass,
            annotationTargetMethod,
            annotationDescription,
            annotationTags,
            annotationTestPoints,
            annotationRequirements,
            annotationDefects,
            annotationTestcases,
            annotationLastUpdateTime,
            annotationLastUpdateAuthor,
            firstSeen != null ? firstSeen.toInstant() : null,
            lastModified != null ? lastModified.toInstant() : null,
            scanSessionId
        );
    }
}


