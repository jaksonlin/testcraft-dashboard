package com.example.annotationextractor.application;

import com.example.annotationextractor.adapters.persistence.jdbc.JdbcScanConfigAdapter;
import com.example.annotationextractor.domain.model.ScanConfig;
import com.example.annotationextractor.domain.model.ScanRepositoryEntry;
import com.example.annotationextractor.web.dto.ScanConfigDto;

import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Application service that orchestrates persistence of scan configuration settings.
 */
@Service
public class ScanConfigService {

    private static final String REPOSITORY_LIST_LABEL = "database-managed";
    private static final String DEFAULT_BRANCH = "main";

    private final JdbcScanConfigAdapter jdbcScanConfigAdapter;

    public ScanConfigService(JdbcScanConfigAdapter jdbcScanConfigAdapter) {
        this.jdbcScanConfigAdapter = jdbcScanConfigAdapter;
    }

    /**
     * Load the current scan configuration including repository entries.
     */
    public ScanConfig getCurrentConfig() throws SQLException {
        JdbcScanConfigAdapter.ScanSettingsRow settings = jdbcScanConfigAdapter.fetchSettings();
        List<ScanRepositoryEntry> entries = jdbcScanConfigAdapter.fetchRepositoryEntries();
        return mapToDomain(settings, entries);
    }

    /**
     * Fetch only the active repository entries for scanning.
     */
    public List<ScanRepositoryEntry> getActiveRepositoryEntries() throws SQLException {
        return jdbcScanConfigAdapter.fetchRepositoryEntries()
                .stream()
                .filter(ScanRepositoryEntry::isActive)
                .collect(Collectors.toList());
    }

    /**
     * Persist updates to the scan configuration settings and repository definitions.
     */
    public ScanConfig updateConfiguration(ScanConfigDto configDto) throws SQLException {
        Objects.requireNonNull(configDto, "configDto");

        JdbcScanConfigAdapter.ScanSettingsRow currentSettings = jdbcScanConfigAdapter.fetchSettings();
        JdbcScanConfigAdapter.ScanSettingsRow mergedSettings = mergeSettings(currentSettings, configDto);
        jdbcScanConfigAdapter.updateSettings(mergedSettings);

        if (configDto.getRepositoryConfigContent() != null) {
            List<ScanRepositoryEntry> parsedEntries = parseRepositoryConfigContent(configDto.getRepositoryConfigContent());
            jdbcScanConfigAdapter.replaceRepositoryEntries(parsedEntries);
        }

        return mapToDomain(mergedSettings, jdbcScanConfigAdapter.fetchRepositoryEntries());
    }

    private ScanConfig mapToDomain(JdbcScanConfigAdapter.ScanSettingsRow settings, List<ScanRepositoryEntry> entries) {
        String repositoryConfigContent = formatRepositoryConfig(entries);
        return new ScanConfig(
                settings.repositoryHubPath(),
                settings.tempCloneMode(),
                settings.maxRepositoriesPerScan(),
                settings.schedulerEnabled(),
                settings.dailyScanCron(),
                REPOSITORY_LIST_LABEL,
                repositoryConfigContent,
                entries,
                normalizeOrganization(settings.organization()),
                normalizeBranch(settings.scanBranch())
        );
    }

    private JdbcScanConfigAdapter.ScanSettingsRow mergeSettings(JdbcScanConfigAdapter.ScanSettingsRow current, ScanConfigDto dto) {
        String repositoryHubPath = dto.getRepositoryHubPath() != null
                ? dto.getRepositoryHubPath().trim()
                : current.repositoryHubPath();

        boolean tempCloneMode = dto.getTempCloneMode() != null
                ? dto.getTempCloneMode()
                : current.tempCloneMode();

        int maxRepositoriesPerScan = dto.getMaxRepositoriesPerScan() != null
                ? dto.getMaxRepositoriesPerScan()
                : current.maxRepositoriesPerScan();

        if (maxRepositoriesPerScan <= 0) {
            throw new IllegalArgumentException("maxRepositoriesPerScan must be greater than 0");
        }

        boolean schedulerEnabled = dto.getSchedulerEnabled() != null
                ? dto.getSchedulerEnabled()
                : current.schedulerEnabled();

        String dailyScanCron = dto.getDailyScanCron() != null && !dto.getDailyScanCron().trim().isEmpty()
                ? dto.getDailyScanCron().trim()
                : current.dailyScanCron();

        String organization = dto.getOrganization() != null
                ? dto.getOrganization().trim()
                : current.organization();

        String scanBranch = dto.getScanBranch() != null && !dto.getScanBranch().trim().isEmpty()
                ? dto.getScanBranch().trim()
                : current.scanBranch();

        return new JdbcScanConfigAdapter.ScanSettingsRow(
                current.id(),
                repositoryHubPath,
                tempCloneMode,
                maxRepositoriesPerScan,
                schedulerEnabled,
                dailyScanCron,
                normalizeOrganization(organization),
                normalizeBranch(scanBranch)
        );
    }

    private String formatRepositoryConfig(List<ScanRepositoryEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return "";
        }
        return entries.stream()
                .filter(ScanRepositoryEntry::isActive)
                .map(entry -> String.join(",",
                        entry.getRepositoryUrl(),
                        entry.getTeamName(),
                        entry.getTeamCode()))
                .collect(Collectors.joining("\n"));
    }

    private List<ScanRepositoryEntry> parseRepositoryConfigContent(String content) {
        if (content == null) {
            return List.of();
        }
        String[] lines = content.split("\\r?\\n");
        Map<String, ScanRepositoryEntry> byUrl = new LinkedHashMap<>();
        List<String> malformedLines = new ArrayList<>();

        for (String rawLine : lines) {
            if (rawLine == null) {
                continue;
            }
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            String[] parts = line.split(",");
            if (parts.length < 3) {
                malformedLines.add(rawLine);
                continue;
            }
            String url = parts[0].trim();
            String teamName = parts[1].trim();
            String teamCode = parts[2].trim();

            if (url.isEmpty() || teamName.isEmpty() || teamCode.isEmpty()) {
                malformedLines.add(rawLine);
                continue;
            }

            byUrl.put(url, new ScanRepositoryEntry(url, teamName, teamCode, true));
        }

        if (!malformedLines.isEmpty()) {
            throw new IllegalArgumentException("Invalid repository config lines: " + malformedLines);
        }

        return new ArrayList<>(byUrl.values());
    }

    private static String normalizeBranch(String branch) {
        if (branch == null || branch.isBlank()) {
            return DEFAULT_BRANCH;
        }
        return branch;
    }

    private static String normalizeOrganization(String organization) {
        if (organization == null) {
            return "";
        }
        return organization.trim();
    }
}

