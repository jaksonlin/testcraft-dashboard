package com.example.annotationextractor.adapters.persistence.jdbc;

import com.example.annotationextractor.database.DatabaseConfig;
import com.example.annotationextractor.domain.model.ScanRepositoryEntry;

import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC adapter that persists scan configuration settings and repository entries.
 */
@Repository
public class JdbcScanConfigAdapter {

    public record ScanSettingsRow(
            long id,
            String repositoryHubPath,
            boolean tempCloneMode,
            int maxRepositoriesPerScan,
            boolean schedulerEnabled,
            String dailyScanCron,
            String organization,
            String scanBranch
    ) {}

    /**
     * Fetch the persisted scan settings, creating a default record if none exists.
     */
    public ScanSettingsRow fetchSettings() throws SQLException {
        try (Connection connection = DatabaseConfig.getConnection()) {
            Optional<ScanSettingsRow> existing = selectSettings(connection);
            if (existing.isPresent()) {
                return existing.get();
            }
            long id = insertDefaultSettings(connection);
            return selectSettings(connection)
                    .orElseThrow(() -> new SQLException("Failed to initialize default scan settings record with id=" + id));
        }
    }

    /**
     * Persist the supplied settings row.
     */
    public void updateSettings(ScanSettingsRow settings) throws SQLException {
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     UPDATE scan_settings
                     SET repository_hub_path = ?,
                         temp_clone_mode = ?,
                         max_repositories_per_scan = ?,
                         scheduler_enabled = ?,
                         daily_scan_cron = ?,
                         organization = ?,
                         scan_branch = ?,
                         updated_at = NOW()
                     WHERE id = ?
                     """)) {
            statement.setString(1, settings.repositoryHubPath());
            statement.setBoolean(2, settings.tempCloneMode());
            statement.setInt(3, settings.maxRepositoriesPerScan());
            statement.setBoolean(4, settings.schedulerEnabled());
            statement.setString(5, settings.dailyScanCron());
            statement.setString(6, settings.organization() == null ? "" : settings.organization());
            statement.setString(7, normalizeBranch(settings.scanBranch()));
            statement.setLong(8, settings.id());
            int updated = statement.executeUpdate();
            if (updated == 0) {
                throw new SQLException("No scan_settings row updated for id=" + settings.id());
            }
        }
    }

    /**
     * Retrieve all repository configuration entries ordered by URL.
     */
    public List<ScanRepositoryEntry> fetchRepositoryEntries() throws SQLException {
        List<ScanRepositoryEntry> entries = new ArrayList<>();
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT repository_url, team_name, team_code, active
                     FROM scan_repository_configs
                     ORDER BY repository_url
                     """)) {
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    entries.add(new ScanRepositoryEntry(
                            rs.getString("repository_url"),
                            rs.getString("team_name"),
                            rs.getString("team_code"),
                            rs.getBoolean("active")
                    ));
                }
            }
        }
        return entries;
    }

    /**
     * Replace the repository entries transactionally.
     */
    public void replaceRepositoryEntries(List<ScanRepositoryEntry> entries) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConfig.getConnection();
            connection.setAutoCommit(false);
            try (Statement delete = connection.createStatement()) {
                delete.executeUpdate("DELETE FROM scan_repository_configs");
            }
            if (entries != null && !entries.isEmpty()) {
                try (PreparedStatement insert = connection.prepareStatement("""
                        INSERT INTO scan_repository_configs (repository_url, team_name, team_code, active, created_at, updated_at)
                        VALUES (?, ?, ?, ?, NOW(), NOW())
                        """)) {
                    for (ScanRepositoryEntry entry : entries) {
                        insert.setString(1, entry.getRepositoryUrl());
                        insert.setString(2, entry.getTeamName());
                        insert.setString(3, entry.getTeamCode());
                        insert.setBoolean(4, entry.isActive());
                        insert.addBatch();
                    }
                    insert.executeBatch();
                }
            }
            connection.commit();
        } catch (SQLException e) {
            rollbackQuietly(connection);
            throw e;
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException ignored) {
                    // ignore
                }
                try {
                    connection.close();
                } catch (SQLException ignored) {
                    // ignore
                }
            }
        }
    }

    private void rollbackQuietly(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException ignored) {
                // best effort rollback
            }
        }
    }

    private Optional<ScanSettingsRow> selectSettings(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT id,
                       repository_hub_path,
                       temp_clone_mode,
                       max_repositories_per_scan,
                       scheduler_enabled,
                       daily_scan_cron,
                       organization,
                       scan_branch
                FROM scan_settings
                ORDER BY id
                LIMIT 1
                """)) {
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new ScanSettingsRow(
                            rs.getLong("id"),
                            rs.getString("repository_hub_path"),
                            rs.getBoolean("temp_clone_mode"),
                            rs.getInt("max_repositories_per_scan"),
                            rs.getBoolean("scheduler_enabled"),
                            rs.getString("daily_scan_cron"),
                            rs.getString("organization"),
                            normalizeBranch(rs.getString("scan_branch"))
                    ));
                }
            }
        }
        return Optional.empty();
    }

    private long insertDefaultSettings(Connection connection) throws SQLException {
        try (PreparedStatement insert = connection.prepareStatement("""
                INSERT INTO scan_settings (repository_hub_path, temp_clone_mode, max_repositories_per_scan, scheduler_enabled, daily_scan_cron, organization, scan_branch)
                VALUES ('./repositories', FALSE, 100, TRUE, '0 0 2 * * ?', '', 'main')
                """, Statement.RETURN_GENERATED_KEYS)) {
            insert.executeUpdate();
            try (ResultSet keys = insert.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        throw new SQLException("Failed to insert default scan settings row");
    }

    private static String normalizeBranch(String branch) {
        if (branch == null || branch.trim().isEmpty()) {
            return "main";
        }
        return branch.trim();
    }
}

