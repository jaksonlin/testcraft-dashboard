package com.example.annotationextractor.adapters.persistence.jdbc;

import com.example.annotationextractor.database.DatabaseConfig;
import com.example.annotationextractor.domain.model.RepositoryRecord;
import com.example.annotationextractor.domain.model.RepositoryDetailRecord;
import com.example.annotationextractor.domain.port.RepositoryRecordPort;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcRepositoryRecordAdapter implements RepositoryRecordPort {

    @Override
    public Optional<RepositoryRecord> findById(Long id) {
        String sql = "SELECT * FROM repositories WHERE id = ?";
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
    public Optional<RepositoryRecord> findByGitUrl(String gitUrl) {
        String sql = "SELECT * FROM repositories WHERE git_url = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, gitUrl);
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
    public List<RepositoryRecord> findAll() {
        String sql = "SELECT * FROM repositories ORDER BY id";
        List<RepositoryRecord> result = new ArrayList<>();
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
    public List<RepositoryRecord> findByTeamId(Long teamId) {
        String sql = "SELECT * FROM repositories WHERE team_id = ? ORDER BY id";
        List<RepositoryRecord> result = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, teamId);
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
    public List<RepositoryDetailRecord> findRepositoryDetails() {
        String sql = """
            SELECT 
                r.id,
                r.repository_name,
                r.repository_path,
                r.git_url,
                r.total_test_classes,
                r.total_test_methods,
                r.total_annotated_methods,
                r.annotation_coverage_rate,
                r.last_scan_date,
                t.team_name,
                t.team_code
            FROM repositories r
            LEFT JOIN teams t ON r.team_id = t.id
            ORDER BY r.annotation_coverage_rate DESC
            """;
        
        List<RepositoryDetailRecord> repositories = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Long id = rs.getLong("id");
                String repositoryName = rs.getString("repository_name");
                String repositoryPath = rs.getString("repository_path");
                String gitUrl = rs.getString("git_url");
                Integer testClasses = rs.getInt("total_test_classes");
                Integer testMethods = rs.getInt("total_test_methods");
                Integer annotatedMethods = rs.getInt("total_annotated_methods");
                Double coverageRate = rs.getDouble("annotation_coverage_rate");
                
                Timestamp lastScan = rs.getTimestamp("last_scan_date");
                LocalDateTime lastScanDateTime = null;
                if (lastScan != null) {
                    lastScanDateTime = lastScan.toLocalDateTime();
                }
                
                String teamName = rs.getString("team_name");
                String teamCode = rs.getString("team_code");
                
                repositories.add(new RepositoryDetailRecord(
                    id, repositoryName, repositoryPath, gitUrl,
                    testClasses, testMethods, annotatedMethods,
                    coverageRate, lastScanDateTime, teamName, teamCode
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        
        return repositories;
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM repositories";
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

    private RepositoryRecord mapRow(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String repositoryName = rs.getString("repository_name");
        String repositoryPath = rs.getString("repository_path");
        String gitUrl = rs.getString("git_url");
        String gitBranch = rs.getString("git_branch");
        String technologyStack = rs.getString("technology_stack");
        Long teamId = rs.getObject("team_id") != null ? rs.getLong("team_id") : null;
        Timestamp first = rs.getTimestamp("first_scan_date");
        Timestamp last = rs.getTimestamp("last_scan_date");
        int totalClasses = rs.getInt("total_test_classes");
        int totalMethods = rs.getInt("total_test_methods");
        int totalAnnotated = rs.getInt("total_annotated_methods");
        double coverage = rs.getDouble("annotation_coverage_rate");
        int testCodeLines = rs.getInt("test_code_lines");
        int testRelatedCodeLines = rs.getInt("test_related_code_lines");

        return new RepositoryRecord(
            id,
            repositoryName,
            repositoryPath,
            gitUrl,
            gitBranch,
            technologyStack,
            teamId,
            first != null ? first.toInstant() : null,
            last != null ? last.toInstant() : null,
            totalClasses,
            totalMethods,
            totalAnnotated,
            coverage,
            testCodeLines,
            testRelatedCodeLines
        );
    }
}


