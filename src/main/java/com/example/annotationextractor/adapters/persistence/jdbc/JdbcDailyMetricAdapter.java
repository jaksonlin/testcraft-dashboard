package com.example.annotationextractor.adapters.persistence.jdbc;

import com.example.annotationextractor.database.DatabaseConfig;
import com.example.annotationextractor.domain.model.DailyMetric;
import com.example.annotationextractor.domain.port.DailyMetricPort;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcDailyMetricAdapter implements DailyMetricPort {

    @Override
    public Optional<DailyMetric> findByDate(LocalDate date) {
        String sql = "SELECT * FROM daily_metrics WHERE metric_date = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(date));
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
    public List<DailyMetric> findRange(LocalDate startInclusive, LocalDate endInclusive) {
        String sql = "SELECT * FROM daily_metrics WHERE metric_date BETWEEN ? AND ? ORDER BY metric_date";
        List<DailyMetric> result = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(startInclusive));
            stmt.setDate(2, Date.valueOf(endInclusive));
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
    public List<DailyMetric> findRecent(int limit) {
        String sql = "SELECT * FROM daily_metrics ORDER BY metric_date DESC LIMIT ?";
        List<DailyMetric> result = new ArrayList<>();
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
        String sql = "SELECT COUNT(*) FROM daily_metrics";
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

    private DailyMetric mapRow(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        LocalDate date = rs.getDate("metric_date").toLocalDate();
        int totalRepositories = rs.getInt("total_repositories");
        int totalTestClasses = rs.getInt("total_test_classes");
        int totalTestMethods = rs.getInt("total_test_methods");
        int totalAnnotatedMethods = rs.getInt("total_annotated_methods");
        double overallCoverageRate = rs.getDouble("overall_coverage_rate");
        int newTestMethods = rs.getInt("new_test_methods");
        int newAnnotatedMethods = rs.getInt("new_annotated_methods");

        return new DailyMetric(
            id,
            date,
            totalRepositories,
            totalTestClasses,
            totalTestMethods,
            totalAnnotatedMethods,
            overallCoverageRate,
            newTestMethods,
            newAnnotatedMethods
        );
    }
}


