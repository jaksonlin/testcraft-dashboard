package com.example.annotationextractor.service;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Comparator;

import org.springframework.stereotype.Service;

import com.example.annotationextractor.application.PersistenceReadFacade;
import com.example.annotationextractor.domain.model.RepositoryDetailRecord;
import com.example.annotationextractor.domain.model.RepositoryRecord;
import com.example.annotationextractor.domain.model.Team;
import com.example.annotationextractor.domain.model.TestMethodDetailRecord;
import com.example.annotationextractor.domain.model.TestClass;
import com.example.annotationextractor.domain.model.TestMethod;
import com.example.annotationextractor.domain.model.ScanSession;
import com.example.annotationextractor.web.dto.PagedResponse;
import com.example.annotationextractor.web.dto.RepositoryDetailDto;
import com.example.annotationextractor.web.dto.RepositoryMetricsDto;
import com.example.annotationextractor.web.dto.RepositorySummaryDto;
import com.example.annotationextractor.web.dto.TestMethodDetailDto;
import com.example.annotationextractor.web.dto.TestClassSummaryDto;
import com.example.annotationextractor.web.dto.GroupedTestMethodResponse;
import com.example.annotationextractor.web.dto.TestMethodSourceDto;

@Service
public class RepositoryDataService {

    private final Optional<PersistenceReadFacade> persistenceReadFacade;

    public RepositoryDataService(Optional<PersistenceReadFacade> persistenceReadFacade) {
        this.persistenceReadFacade = persistenceReadFacade;
    }

    /**
     * Retrieve the source code for the class that owns a specific test method.
     */
    /**
     * Retrieve the source code for the class that owns a specific test method.
     */
    public Optional<TestMethodSourceDto> getTestMethodSource(Long testMethodId) {
        if (persistenceReadFacade.isEmpty() || testMethodId == null) {
            return Optional.empty();
        }

        try {
            PersistenceReadFacade facade = persistenceReadFacade.get();

            Optional<TestMethod> maybeMethod = facade.getTestMethodById(testMethodId);
            if (maybeMethod.isEmpty()) {
                return Optional.empty();
            }

            TestMethod method = maybeMethod.get();
            if (method.getTestClassId() == null) {
                return Optional.empty();
            }

            Optional<TestClass> maybeClass = facade.getTestClassById(method.getTestClassId());
            if (maybeClass.isEmpty()) {
                return Optional.empty();
            }

            TestClass testClass = maybeClass.get();
            String content = testClass.getTestClassContent();

            // If content is missing in DB, try to read from disk
            if (content == null || content.isBlank()) {
                try {
                    Optional<RepositoryRecord> maybeRepo = facade.getRepositoryById(testClass.getRepositoryId());
                    if (maybeRepo.isPresent()) {
                        RepositoryRecord repo = maybeRepo.get();
                        String repoPath = repo.getRepositoryPath();
                        String filePath = testClass.getFilePath();

                        if (repoPath != null && filePath != null) {
                            java.nio.file.Path fullPath = java.nio.file.Paths.get(repoPath, filePath);
                            if (java.nio.file.Files.exists(fullPath)) {
                                content = java.nio.file.Files.readString(fullPath);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Failed to read source file from disk: " + e.getMessage());
                }
            }

            if (content == null || content.isBlank()) {
                return Optional.empty();
            }

            TestMethodSourceDto dto = new TestMethodSourceDto();
            dto.setTestMethodId(method.getId());
            dto.setTestMethodName(method.getMethodName());
            dto.setMethodLine(method.getLineNumber());

            dto.setTestClassId(testClass.getId());
            dto.setTestClassName(testClass.getClassName());
            dto.setPackageName(testClass.getPackageName());
            dto.setFilePath(testClass.getFilePath());
            dto.setClassLineNumber(testClass.getClassLineNumber());
            dto.setClassContent(content);

            return Optional.of(dto);
        } catch (Exception ex) {
            System.err.println("Error fetching test method source: " + ex.getMessage());
            ex.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Get all repository metrics (for repository management view)
     */
    public List<RepositoryMetricsDto> getAllRepositoryMetrics() {
        if (persistenceReadFacade.isPresent()) {
            try {
                List<RepositoryRecord> repositories = persistenceReadFacade.get().listAllRepositories();
                List<Team> teams = persistenceReadFacade.get().listTeams();

                return repositories.stream()
                        .map(repo -> convertToRepositoryMetricsDto(repo, teams))
                        .collect(Collectors.toList());

            } catch (Exception e) {
                return List.of();
            }
        }
        return List.of();
    }

    /**
     * Get repositories with pagination and filtering
     */
    public PagedResponse<RepositoryMetricsDto> getRepositoriesPaginated(
            int page, int size, String search, String team, String coverage,
            String testMethods, String lastScan, String sortBy, String sortOrder) {
        if (persistenceReadFacade.isPresent()) {
            try {
                List<RepositoryRecord> allRepositories = persistenceReadFacade.get().listAllRepositories();
                List<Team> teams = persistenceReadFacade.get().listTeams();

                // Convert to DTOs
                List<RepositoryMetricsDto> repositories = allRepositories.stream()
                        .map(repo -> convertToRepositoryMetricsDto(repo, teams))
                        .collect(Collectors.toList());

                // Apply filters
                List<RepositoryMetricsDto> filteredRepositories = repositories.stream()
                        .filter(repo -> {
                            // Search filter
                            if (search != null && !search.trim().isEmpty()) {
                                String searchLower = search.toLowerCase();
                                if (!repo.getRepositoryName().toLowerCase().contains(searchLower) &&
                                        !repo.getTeamName().toLowerCase().contains(searchLower) &&
                                        !repo.getGitUrl().toLowerCase().contains(searchLower)) {
                                    return false;
                                }
                            }

                            // Team filter
                            if (team != null && !team.trim().isEmpty()) {
                                if (!repo.getTeamName().equalsIgnoreCase(team)) {
                                    return false;
                                }
                            }

                            // Coverage filter
                            if (coverage != null && !coverage.trim().isEmpty()) {
                                if (!matchesCoverageRange(repo.getCoverageRate(), coverage)) {
                                    return false;
                                }
                            }

                            // Test Methods filter
                            if (testMethods != null && !testMethods.trim().isEmpty()) {
                                if (!matchesTestMethodsRange(repo.getTestMethodCount(), testMethods)) {
                                    return false;
                                }
                            }

                            // Last Scan filter
                            if (lastScan != null && !lastScan.trim().isEmpty()) {
                                if (!matchesLastScanRange(
                                        repo.getLastScanDate() != null ? repo.getLastScanDate().toString() : null,
                                        lastScan)) {
                                    return false;
                                }
                            }

                            return true;
                        })
                        .collect(Collectors.toList());

                // Apply sorting
                if (sortBy != null && !sortBy.trim().isEmpty()) {
                    Comparator<RepositoryMetricsDto> comparator = getComparator(sortBy);
                    if ("desc".equalsIgnoreCase(sortOrder)) {
                        comparator = comparator.reversed();
                    }
                    filteredRepositories.sort(comparator);
                }

                // Apply pagination
                int totalElements = filteredRepositories.size();
                int totalPages = (int) Math.ceil((double) totalElements / size);
                int startIndex = page * size;
                int endIndex = Math.min(startIndex + size, totalElements);

                List<RepositoryMetricsDto> paginatedRepositories = filteredRepositories.subList(startIndex, endIndex);

                return new PagedResponse<>(paginatedRepositories, page, size, totalElements);

            } catch (Exception e) {
                System.err.println("Error fetching paginated repositories: " + e.getMessage());
                e.printStackTrace();
                return new PagedResponse<>(List.of(), page, size, 0);
            }
        }
        return new PagedResponse<>(List.of(), page, size, 0);
    }

    private boolean matchesCoverageRange(double coverage, String range) {
        switch (range.toLowerCase()) {
            case "high":
                return coverage >= 80;
            case "medium":
                return coverage >= 50 && coverage < 80;
            case "low":
                return coverage < 50;
            default:
                return true;
        }
    }

    private boolean matchesTestMethodsRange(int testMethodCount, String range) {
        switch (range.toLowerCase()) {
            case "high":
                return testMethodCount >= 100;
            case "medium":
                return testMethodCount >= 20 && testMethodCount < 100;
            case "low":
                return testMethodCount < 20;
            default:
                return true;
        }
    }

    private boolean matchesLastScanRange(String lastScanDate, String range) {
        if (lastScanDate == null || lastScanDate.trim().isEmpty()) {
            return false;
        }

        try {
            java.time.LocalDateTime scanDate = java.time.LocalDateTime.parse(lastScanDate);
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            long diffDays = java.time.Duration.between(scanDate, now).toDays();

            switch (range.toLowerCase()) {
                case "today":
                    return diffDays == 0;
                case "week":
                    return diffDays <= 7;
                case "month":
                    return diffDays <= 30;
                case "older":
                    return diffDays > 30;
                default:
                    return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private Comparator<RepositoryMetricsDto> getComparator(String sortBy) {
        switch (sortBy.toLowerCase()) {
            case "name":
                return Comparator.comparing(RepositoryMetricsDto::getRepositoryName);
            case "team":
                return Comparator.comparing(RepositoryMetricsDto::getTeamName);
            case "coverage":
                return Comparator.comparing(RepositoryMetricsDto::getCoverageRate);
            case "testmethods":
                return Comparator.comparing(RepositoryMetricsDto::getTestMethodCount);
            case "lastscan":
                return Comparator.comparing(RepositoryMetricsDto::getLastScanDate);
            default:
                return Comparator.comparing(RepositoryMetricsDto::getRepositoryName);
        }
    }

    private RepositoryMetricsDto convertToRepositoryMetricsDto(RepositoryRecord repo, List<Team> teams) {
        RepositoryMetricsDto dto = new RepositoryMetricsDto(repo.getId(), repo.getRepositoryName(), repo.getGitUrl());
        dto.setRepositoryId(repo.getId()); // Ensure repositoryId is set

        // Find team name
        String teamName = teams.stream()
                .filter(team -> team.getId().equals(repo.getTeamId()))
                .findFirst()
                .map(Team::getTeamName)
                .orElse("Unknown");

        dto.setTeamName(teamName);
        dto.setRepositoryPath(repo.getRepositoryPath());
        dto.setGitBranch(repo.getGitBranch());
        dto.setTechnologyStack(repo.getTechnologyStack());
        dto.setTestClassCount(repo.getTotalTestClasses());
        dto.setTestMethodCount(repo.getTotalTestMethods());
        dto.setAnnotatedMethodCount(repo.getTotalAnnotatedMethods());
        dto.setCoverageRate(repo.getAnnotationCoverageRate());
        dto.setFirstScanDate(repo.getFirstScanDate()
                .atZone(ZoneId.systemDefault()).toLocalDateTime());
        dto.setLastScanDate(repo.getLastScanDate()
                .atZone(ZoneId.systemDefault()).toLocalDateTime());

        return dto;
    }

    /**
     * Get repository metrics for a specific team
     */
    public List<RepositoryMetricsDto> getRepositoryMetricsByTeamId(Long teamId) {
        if (persistenceReadFacade.isPresent()) {
            try {
                List<RepositoryRecord> repositories = persistenceReadFacade.get().listRepositoriesByTeam(teamId);
                List<Team> teams = persistenceReadFacade.get().listTeams();

                return repositories.stream()
                        .map(repo -> convertToRepositoryMetricsDto(repo, teams))
                        .collect(Collectors.toList());

            } catch (Exception e) {
                return List.of();
            }
        }
        return List.of();
    }

    /**
     * Get repository details matching Excel Repository Details sheet format
     */
    public List<RepositoryDetailDto> getRepositoryDetails() {
        if (persistenceReadFacade.isPresent()) {
            try {
                if (!persistenceReadFacade.isPresent()) {
                    return new ArrayList<>();
                }

                List<RepositoryDetailRecord> records = persistenceReadFacade.get().listRepositoryDetails();

                return records.stream()
                        .map(this::convertToRepositoryDetailDto)
                        .collect(Collectors.toList());

            } catch (Exception e) {
                return List.of();
            }
        }
        return List.of();
    }

    /**
     * Convert RepositoryDetailRecord to RepositoryDetailDto
     */
    private RepositoryDetailDto convertToRepositoryDetailDto(RepositoryDetailRecord record) {
        RepositoryDetailDto dto = new RepositoryDetailDto();
        dto.setId(record.getId());
        dto.setRepository(record.getRepositoryName());
        dto.setPath(record.getRepositoryPath());
        dto.setGitUrl(record.getGitUrl());
        dto.setTestClasses(record.getTestClasses());
        dto.setTestMethodCount(record.getTestMethodCount());
        dto.setAnnotatedMethods(record.getAnnotatedMethods());
        dto.setCoverageRate(record.getCoverageRate());
        dto.setLastScan(record.getLastScan());
        dto.setTeamName(record.getTeamName());
        dto.setTeamCode(record.getTeamCode());
        return dto;
    }

    /**
     * Get test classes for a specific repository
     */
    public List<TestClassSummaryDto> getRepositoryClasses(Long repositoryId) {
        if (persistenceReadFacade.isPresent()) {
            try {
                // Get the latest scan session for this specific repository
                Optional<Long> latestScanId = persistenceReadFacade.get()
                        .getLatestScanSessionIdForRepository(repositoryId);
                if (latestScanId.isEmpty()) {
                    System.err.println("No completed scan session found for repository: " + repositoryId);
                    return List.of();
                }

                Long scanSessionId = latestScanId.get();
                List<TestClass> classes = persistenceReadFacade.get()
                        .listClassesByRepositoryIdAndScanSessionId(repositoryId, scanSessionId);

                // Filter classes by scan session ID (redundant but safe)
                List<TestClass> filteredClasses = classes.stream()
                        .filter(testClass -> testClass.getScanSessionId() != null &&
                                testClass.getScanSessionId().equals(scanSessionId))
                        .collect(Collectors.toList());

                return filteredClasses.stream()
                        .map(this::convertToTestClassSummaryDto)
                        .collect(Collectors.toList());
            } catch (Exception e) {
                System.err.println("Error fetching repository classes: " + e.getMessage());
                e.printStackTrace();
                return List.of();
            }
        } else {
            System.err.println("PersistenceReadFacade is not available - database may not be configured");
            return List.of();
        }
    }

    /**
     * Get test classes for a specific repository (paginated)
     */
    public PagedResponse<TestClassSummaryDto> getRepositoryClassesPaginated(Long repositoryId, int page, int size,
            String className, Boolean annotated) {
        if (persistenceReadFacade.isPresent()) {
            try {
                // Get the latest scan session for this specific repository
                Optional<Long> latestScanId = persistenceReadFacade.get()
                        .getLatestScanSessionIdForRepository(repositoryId);
                if (latestScanId.isEmpty()) {
                    System.err.println("No completed scan session found for repository: " + repositoryId);
                    return new PagedResponse<TestClassSummaryDto>(List.of(), page, size, 0);
                }

                Long scanSessionId = latestScanId.get();
                List<TestClass> allClasses = persistenceReadFacade.get()
                        .listClassesByRepositoryIdAndScanSessionId(repositoryId, scanSessionId);

                // Filter classes by scan session ID (redundant but safe)
                List<TestClass> filteredClasses = allClasses.stream()
                        .filter(testClass -> testClass.getScanSessionId() != null &&
                                testClass.getScanSessionId().equals(scanSessionId))
                        .collect(Collectors.toList());

                // Apply additional filters
                List<TestClass> filteredByCriteria = filteredClasses.stream()
                        .filter(testClass -> {
                            // Class name filter
                            if (className != null && !className.trim().isEmpty()) {
                                if (!testClass.getClassName().toLowerCase().contains(className.toLowerCase())) {
                                    return false;
                                }
                            }

                            // Annotation filter - check if any method in the class is annotated
                            if (annotated != null) {
                                try {
                                    List<TestMethodDetailRecord> methods = persistenceReadFacade.get()
                                            .listTestMethodDetailsByClassId(testClass.getId(), 1000);
                                    boolean hasAnnotatedMethod = methods.stream()
                                            .anyMatch(method -> method.getAnnotationTitle() != null
                                                    && !method.getAnnotationTitle().trim().isEmpty() &&
                                                    !method.getAnnotationTitle().equals("No title"));

                                    if (annotated && !hasAnnotatedMethod)
                                        return false;
                                    if (!annotated && hasAnnotatedMethod)
                                        return false;
                                } catch (Exception e) {
                                    // If we can't check methods, include the class
                                }
                            }

                            return true;
                        })
                        .collect(Collectors.toList());

                // Convert to DTOs
                List<TestClassSummaryDto> classDtos = filteredByCriteria.stream()
                        .map(this::convertToTestClassSummaryDto)
                        .collect(Collectors.toList());

                // Apply pagination
                int totalElements = classDtos.size();
                int startIndex = page * size;
                int endIndex = Math.min(startIndex + size, totalElements);

                List<TestClassSummaryDto> paginatedClasses = classDtos.subList(startIndex, endIndex);

                return new PagedResponse<TestClassSummaryDto>(paginatedClasses, page, size, totalElements);

            } catch (Exception e) {
                System.err.println("Error fetching paginated repository classes: " + e.getMessage());
                e.printStackTrace();
                return new PagedResponse<TestClassSummaryDto>(List.of(), page, size, 0);
            }
        } else {
            System.err.println("PersistenceReadFacade is not available - database may not be configured");
            return new PagedResponse<TestClassSummaryDto>(List.of(), page, size, 0);
        }
    }

    /**
     * Get test methods for a specific class in a repository
     */
    public List<TestMethodDetailDto> getTestMethodsByClassId(Long classId, Integer limit) {
        if (persistenceReadFacade.isPresent()) {
            try {
                // Get all test methods for the repository, then filter by class and scan
                // session
                List<TestMethodDetailRecord> records = persistenceReadFacade.get()
                        .listTestMethodDetailsByClassId(classId, limit);

                return records.stream()
                        .filter(record -> record.getTestClassName() != null &&
                                record.getRepositoryName() != null)
                        .map(this::convertToTestMethodDetailDto)
                        .collect(Collectors.toList());
            } catch (Exception e) {
                System.err.println("Error fetching class methods: " + e.getMessage());
                e.printStackTrace();
                return List.of();
            }
        } else {
            System.err.println("PersistenceReadFacade is not available - database may not be configured");
            return List.of();
        }
    }

    /**
     * Convert TestClass to TestClassSummaryDto
     */
    private TestClassSummaryDto convertToTestClassSummaryDto(TestClass testClass) {
        TestClassSummaryDto dto = new TestClassSummaryDto();
        dto.setId(testClass.getId());
        dto.setClassName(testClass.getClassName());
        dto.setPackageName(testClass.getPackageName());
        dto.setFilePath(testClass.getFilePath());
        dto.setTestMethodCount(testClass.getTotalTestMethods());
        dto.setAnnotatedMethodCount(testClass.getAnnotatedTestMethods());
        dto.setCoverageRate(testClass.getCoverageRate());
        dto.setLastModifiedDate(testClass.getLastModifiedDate().atZone(ZoneId.systemDefault()).toLocalDateTime());
        return dto;
    }

    /**
     * Convert TestMethodDetailRecord to TestMethodDetailDto
     */
    private TestMethodDetailDto convertToTestMethodDetailDto(TestMethodDetailRecord record) {
        TestMethodDetailDto dto = new TestMethodDetailDto();
        dto.setId(record.getId());
        dto.setRepository(record.getRepositoryName());
        dto.setTestClass(record.getTestClassName());
        dto.setTestMethod(record.getTestMethodName());
        dto.setLine(record.getLineNumber());
        dto.setTitle(record.getAnnotationTitle());
        dto.setAuthor(record.getAnnotationAuthor());
        dto.setStatus(record.getAnnotationStatus());
        dto.setTargetClass(record.getAnnotationTargetClass());
        dto.setTargetMethod(record.getAnnotationTargetMethod());
        dto.setDescription(record.getAnnotationDescription());
        dto.setTestPoints(record.getAnnotationTestPoints());
        dto.setTags(record.getAnnotationTags());
        dto.setRequirements(record.getAnnotationRequirements());
        dto.setTestCaseIds(record.getAnnotationTestcases());
        dto.setDefects(record.getAnnotationDefects());
        dto.setLastModified(record.getAnnotationLastUpdateTime());
        dto.setLastUpdateAuthor(record.getAnnotationLastUpdateAuthor());
        // Ensure team fields are never null to maintain consistent column structure
        dto.setTeamName(record.getTeamName() != null ? record.getTeamName() : "");
        dto.setTeamCode(record.getTeamCode() != null ? record.getTeamCode() : "");
        dto.setGitUrl(record.getGitUrl() != null ? record.getGitUrl() : "");
        return dto;
    }

    public List<RepositorySummaryDto> getTopRepositories(List<RepositoryRecord> repositories, List<Team> teams,
            int limit) {
        return repositories.stream()
                .map(repo -> {
                    RepositorySummaryDto summary = new RepositorySummaryDto(repo.getId(), repo.getRepositoryName(),
                            repo.getGitUrl());

                    // Find team name
                    String teamName = teams.stream()
                            .filter(team -> team.getId().equals(repo.getTeamId()))
                            .findFirst()
                            .map(Team::getTeamName)
                            .orElse("Unknown");

                    summary.setTeamName(teamName);
                    summary.setTestClassCount(repo.getTotalTestClasses());
                    summary.setTestMethodCount(repo.getTotalTestMethods());
                    summary.setAnnotatedMethodCount(repo.getTotalAnnotatedMethods());
                    summary.setCoverageRate(repo.getAnnotationCoverageRate());
                    summary.setLastScanDate(repo.getLastScanDate()
                            .atZone(ZoneId.systemDefault()).toLocalDateTime());

                    return summary;
                })
                .sorted((a, b) -> Double.compare(b.getCoverageRate(), a.getCoverageRate()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Get all test method details for dashboard overview
     */
    public List<TestMethodDetailDto> getAllTestMethodDetails(Integer limit) {
        if (persistenceReadFacade.isPresent()) {
            try {
                // Use aggregated scan session IDs (latest per repository)
                Map<Long, Long> latestSessions = getLatestScanSessionIds();
                if (latestSessions.isEmpty()) {
                    System.err.println("No completed scan sessions found");
                    return List.of();
                }

                System.err.println("Using " + latestSessions.size() + " scan sessions for all test methods");

                // Use the filter-based method which supports list of session IDs
                List<TestMethodDetailRecord> records = persistenceReadFacade.get()
                        .listTestMethodDetailsWithFilters(
                                latestSessions,
                                null, null, null, null, null, null, null,
                                0, limit);
                System.err.println("Found " + records.size() + " total test method records");
                return records.stream()
                        .map(this::convertToTestMethodDetailDto)
                        .collect(Collectors.toList());
            } catch (Exception e) {
                System.err.println("Error fetching all test method details: " + e.getMessage());
                e.printStackTrace();
                return List.of();
            }
        } else {
            System.err.println("PersistenceReadFacade is not available - database may not be configured");
            return List.of();
        }
    }

    /**
     * Get test methods for a specific repository
     */
    public List<TestMethodDetailDto> getTestMethodsByRepositoryId(Long repositoryId, Integer limit) {
        if (persistenceReadFacade.isPresent()) {
            try {
                // Find the latest scan session that has data for this repository
                // Long scanSessionId =
                // findLatestScanSessionWithDataForRepository(repositoryId);
                // Get the latest scan session for this specific repository
                Optional<Long> latestScanId = persistenceReadFacade.get()
                        .getLatestScanSessionIdForRepository(repositoryId);
                if (latestScanId.isEmpty()) {
                    System.err.println("No scan session found with data for repository: " + repositoryId);
                    return List.of();
                }
                Long scanSessionId = latestScanId.get();
                System.err.println("Using scan session ID: " + scanSessionId + " for repository: " + repositoryId);
                List<TestMethodDetailRecord> records = persistenceReadFacade.get()
                        .listTestMethodDetailsByRepositoryIdAndScanSessionId(repositoryId, scanSessionId, limit);
                System.err.println("Found " + records.size() + " test method records for repository " + repositoryId);
                return records.stream()
                        .map(this::convertToTestMethodDetailDto)
                        .collect(Collectors.toList());
            } catch (Exception e) {
                System.err.println("Error fetching test methods by repository: " + e.getMessage());
                e.printStackTrace();
                return List.of();
            }
        } else {
            System.err.println("PersistenceReadFacade is not available - database may not be configured");
            return List.of();
        }
    }

    /**
     * Find the latest scan session that has data for a specific repository
     */
    private Long findLatestScanSessionWithDataForRepository(Long repositoryId) {
        try {
            // Check scan sessions from latest to oldest
            List<ScanSession> recentSessions = persistenceReadFacade.get().recentScanSessions(20);

            for (ScanSession session : recentSessions) {
                if (session.getScanStatus().equals("COMPLETED")) {
                    // Check for test classes first (more reliable indicator)
                    List<TestClass> classes = persistenceReadFacade.get()
                            .listClassesByRepositoryIdAndScanSessionId(repositoryId, session.getId());
                    if (!classes.isEmpty()) {
                        System.err.println("Found test classes for repository " + repositoryId + " in scan session "
                                + session.getId());
                        return session.getId();
                    }

                    // Fallback: check for test methods
                    List<TestMethodDetailRecord> methods = persistenceReadFacade.get()
                            .listTestMethodDetailsByRepositoryIdAndScanSessionId(repositoryId, session.getId(), 1);
                    if (!methods.isEmpty()) {
                        System.err.println("Found test methods for repository " + repositoryId + " in scan session "
                                + session.getId());
                        return session.getId();
                    }
                }
            }

            System.err.println("No scan session found with data for repository: " + repositoryId);
            return null;
        } catch (Exception e) {
            System.err.println("Error finding scan session for repository: " + e.getMessage());
            return null;
        }
    }

    public List<TestMethodDetailDto> getTestMethodDetailsByTeamId(Long teamId, Integer limit) {
        if (persistenceReadFacade.isPresent()) {
            try {
                // Use aggregated scan session IDs (latest per repository)
                Map<Long, Long> latestSessions = getLatestScanSessionIds();
                if (latestSessions.isEmpty()) {
                    System.err.println("No completed scan sessions found");
                    return List.of();
                }

                // Get team name to use with filter
                String teamName = persistenceReadFacade.get().listTeams().stream()
                        .filter(t -> t.getId().equals(teamId))
                        .findFirst()
                        .map(Team::getTeamName)
                        .orElse(null);

                if (teamName == null) {
                    System.err.println("Team not found for ID: " + teamId);
                    return List.of();
                }

                System.err.println("Using " + latestSessions.size() + " scan sessions for team: " + teamName);

                // Use the filter-based method which supports list of session IDs
                List<TestMethodDetailRecord> records = persistenceReadFacade.get()
                        .listTestMethodDetailsWithFilters(
                                latestSessions,
                                teamName,
                                null, null, null, null, null, null,
                                0, limit);
                System.err.println("Found " + records.size() + " test method records");
                return records.stream()
                        .map(this::convertToTestMethodDetailDto)
                        .collect(Collectors.toList());
            } catch (Exception e) {
                System.err.println("Error fetching test method details by team: " + e.getMessage());
                e.printStackTrace();
                return List.of();
            }
        } else {
            System.err.println("PersistenceReadFacade is not available - database may not be configured");
            return List.of();
        }
    }

    /**
     * Get all test method details grouped by team and class for hierarchical
     * display
     * This method provides pre-grouped data to avoid performance issues on the
     * frontend
     * Filters are applied at database level for optimal performance
     */
    public GroupedTestMethodResponse getAllTestMethodDetailsGrouped(Integer limit, String searchTerm,
            Boolean annotated) {
        if (persistenceReadFacade.isPresent()) {
            try {
                // Get latest scan session IDs for ALL repositories
                Map<Long, Long> latestSessions = getLatestScanSessionIds();
                if (latestSessions.isEmpty()) {
                    System.err.println("No scan sessions found for any repository");
                    return new GroupedTestMethodResponse(List.of(),
                            new GroupedTestMethodResponse.SummaryDto(0, 0, 0, 0, 0.0));
                }

                System.err.println("Using " + latestSessions.size() + " scan sessions for grouped test methods");

                // Apply filters at database level (NO client-side filtering)
                List<TestMethodDetailRecord> records = persistenceReadFacade.get()
                        .listTestMethodDetailsWithFilters(
                                latestSessions,
                                null, // teamName
                                null, // repositoryName
                                null, // packageName
                                null, // className
                                annotated,
                                searchTerm,
                                null, // codePattern
                                null, // offset
                                limit);

                System.err.println("Found " + records.size() + " filtered test method records for grouping");

                return groupTestMethodDetails(records);

            } catch (Exception e) {
                System.err.println("Error fetching grouped test method details: " + e.getMessage());
                e.printStackTrace();
                return new GroupedTestMethodResponse(List.of(),
                        new GroupedTestMethodResponse.SummaryDto(0, 0, 0, 0, 0.0));
            }
        } else {
            System.err.println("PersistenceReadFacade is not available - database may not be configured");
            return new GroupedTestMethodResponse(List.of(),
                    new GroupedTestMethodResponse.SummaryDto(0, 0, 0, 0, 0.0));
        }
    }

    /**
     * Group test method details by team and class
     */
    private GroupedTestMethodResponse groupTestMethodDetails(List<TestMethodDetailRecord> records) {
        // Group by team
        Map<String, List<TestMethodDetailRecord>> teamGroups = records.stream()
                .collect(Collectors
                        .groupingBy(record -> record.getTeamName() != null ? record.getTeamName() : "Unknown Team"));

        List<GroupedTestMethodResponse.TeamGroupDto> teamDtos = new ArrayList<>();
        int totalTeams = teamGroups.size();
        int totalClasses = 0;
        int totalMethods = records.size();
        int totalAnnotatedMethods = 0;

        for (Map.Entry<String, List<TestMethodDetailRecord>> teamEntry : teamGroups.entrySet()) {
            String teamName = teamEntry.getKey();
            List<TestMethodDetailRecord> teamMethods = teamEntry.getValue();

            // Get team code from first record
            String teamCode = teamMethods.isEmpty() ? ""
                    : (teamMethods.get(0).getTeamCode() != null ? teamMethods.get(0).getTeamCode() : "");

            // Group by class within team
            Map<String, List<TestMethodDetailRecord>> classGroups = teamMethods.stream()
                    .collect(Collectors.groupingBy(
                            record -> (record.getRepositoryName() != null ? record.getRepositoryName() : "Unknown")
                                    + "." +
                                    (record.getTestClassName() != null ? record.getTestClassName() : "Unknown")));

            List<GroupedTestMethodResponse.ClassGroupDto> classDtos = new ArrayList<>();
            int teamTotalClasses = classGroups.size();
            int teamTotalMethods = teamMethods.size();
            int teamAnnotatedMethods = 0;

            for (Map.Entry<String, List<TestMethodDetailRecord>> classEntry : classGroups.entrySet()) {
                String classKey = classEntry.getKey();
                List<TestMethodDetailRecord> classMethods = classEntry.getValue();

                // Parse class key to get repository and class name
                String[] parts = classKey.split("\\.", 2);
                String repository = parts.length > 0 ? parts[0] : "Unknown";
                String className = parts.length > 1 ? parts[1] : "Unknown";

                // Convert to DTOs
                List<TestMethodDetailDto> methodDtos = classMethods.stream()
                        .map(this::convertToTestMethodDetailDto)
                        .collect(Collectors.toList());

                // Calculate class summary
                int classTotalMethods = classMethods.size();
                int classAnnotatedMethods = (int) classMethods.stream()
                        .filter(record -> record.getAnnotationTitle() != null
                                && !record.getAnnotationTitle().trim().isEmpty())
                        .count();
                double classCoverageRate = classTotalMethods > 0
                        ? (double) classAnnotatedMethods / classTotalMethods * 100
                        : 0.0;

                GroupedTestMethodResponse.ClassSummaryDto classSummary = new GroupedTestMethodResponse.ClassSummaryDto(
                        classTotalMethods, classAnnotatedMethods, classCoverageRate);

                GroupedTestMethodResponse.ClassGroupDto classDto = new GroupedTestMethodResponse.ClassGroupDto(
                        className, "", repository, methodDtos, classSummary);

                classDtos.add(classDto);
                teamAnnotatedMethods += classAnnotatedMethods;
            }

            // Calculate team summary
            double teamCoverageRate = teamTotalMethods > 0 ? (double) teamAnnotatedMethods / teamTotalMethods * 100
                    : 0.0;

            GroupedTestMethodResponse.TeamSummaryDto teamSummary = new GroupedTestMethodResponse.TeamSummaryDto(
                    teamTotalClasses, teamTotalMethods, teamAnnotatedMethods, teamCoverageRate);

            GroupedTestMethodResponse.TeamGroupDto teamDto = new GroupedTestMethodResponse.TeamGroupDto(teamName,
                    teamCode, classDtos, teamSummary);

            teamDtos.add(teamDto);
            totalClasses += teamTotalClasses;
            totalAnnotatedMethods += teamAnnotatedMethods;
        }

        // Calculate overall summary
        double overallCoverageRate = totalMethods > 0 ? (double) totalAnnotatedMethods / totalMethods * 100 : 0.0;

        GroupedTestMethodResponse.SummaryDto summary = new GroupedTestMethodResponse.SummaryDto(totalTeams,
                totalClasses, totalMethods, totalAnnotatedMethods, overallCoverageRate);

        return new GroupedTestMethodResponse(teamDtos, summary);
    }

    /**
     * Get global test method statistics (not limited to current page)
     * ALL FILTERING IS DONE AT DATABASE LEVEL - NO CLIENT-SIDE FILTERING
     * Returns accurate totals for filtering/decision making
     */
    public Map<String, Object> getGlobalTestMethodStats(
            String organization, Long teamId, String repositoryName, Boolean annotated) {

        if (persistenceReadFacade.isPresent()) {
            try {
                // Get latest scan session IDs for ALL repositories
                Map<Long, Long> latestSessions = getLatestScanSessionIds();
                if (latestSessions.isEmpty()) {
                    System.err.println("No scan sessions found for any repository");
                    return Map.of(
                            "totalMethods", 0,
                            "totalAnnotated", 0,
                            "totalNotAnnotated", 0,
                            "coverageRate", 0.0);
                }

                // Get total count with filters (from database)
                long totalMethods = persistenceReadFacade.get()
                        .countTestMethodDetailsWithFilters(
                                latestSessions,
                                null, // teamName (TODO: convert teamId to teamName if needed)
                                repositoryName,
                                null, // packageName
                                null, // className
                                annotated,
                                null, // searchTerm
                                null // codePattern
                        );

                // Get annotated count with filters (from database)
                long totalAnnotated = persistenceReadFacade.get()
                        .countTestMethodDetailsWithFilters(
                                latestSessions,
                                null, // teamName
                                repositoryName,
                                null, // packageName
                                null, // className
                                true, // annotated only
                                null, // searchTerm
                                null // codePattern
                        );

                long totalNotAnnotated = totalMethods - totalAnnotated;
                double coverageRate = totalMethods > 0 ? (double) totalAnnotated / totalMethods * 100.0 : 0.0;

                System.err.println("Database-level stats: " + totalMethods + " total, " +
                        totalAnnotated + " annotated (" + String.format("%.1f", coverageRate) + "% coverage)");

                return Map.of(
                        "totalMethods", (int) totalMethods,
                        "totalAnnotated", (int) totalAnnotated,
                        "totalNotAnnotated", (int) totalNotAnnotated,
                        "coverageRate", coverageRate);

            } catch (Exception e) {
                System.err.println("Error calculating global test method stats: " + e.getMessage());
                e.printStackTrace();
                return Map.of(
                        "totalMethods", 0,
                        "totalAnnotated", 0,
                        "totalNotAnnotated", 0,
                        "coverageRate", 0.0);
            }
        } else {
            return Map.of(
                    "totalMethods", 0,
                    "totalAnnotated", 0,
                    "totalNotAnnotated", 0,
                    "coverageRate", 0.0);
        }
    }

    /**
     * Get test method details with pagination and filtering
     * ALL FILTERING IS DONE AT DATABASE LEVEL - NO CLIENT-SIDE FILTERING
     * This ensures optimal performance even with 200,000+ test methods
     */
    public PagedResponse<TestMethodDetailDto> getTestMethodDetailsPaginated(
            int page, int size, String organization, String teamName, String repositoryName,
            String packageName, String className, Boolean annotated, String codePattern) {

        if (persistenceReadFacade.isPresent()) {
            try {
                // Get latest scan session IDs for ALL repositories
                Map<Long, Long> latestSessions = getLatestScanSessionIds();
                if (latestSessions.isEmpty()) {
                    System.err.println("No scan sessions found for any repository");
                    return new PagedResponse<>(List.of(), page, size, 0);
                }

                int offset = page * size;

                // Get filtered data directly from database (NO client-side filtering)
                List<TestMethodDetailRecord> records = persistenceReadFacade.get()
                        .listTestMethodDetailsWithFilters(
                                latestSessions,
                                teamName,
                                repositoryName,
                                packageName,
                                className,
                                annotated,
                                null, // searchTerm not used in paginated endpoint yet
                                codePattern, // codePattern for filtering by target class/method
                                offset,
                                size);

                // Get accurate count of filtered results (from database, not memory)
                long totalCount = persistenceReadFacade.get()
                        .countTestMethodDetailsWithFilters(
                                latestSessions,
                                teamName,
                                repositoryName,
                                packageName,
                                className,
                                annotated,
                                null, // searchTerm not used in paginated endpoint yet
                                codePattern // codePattern for filtering by target class/method
                        );

                // Convert to DTOs
                List<TestMethodDetailDto> methodDtos = records.stream()
                        .map(this::convertToTestMethodDetailDto)
                        .collect(Collectors.toList());

                System.err.println("Database-level filtering: returned " + records.size() +
                        " records (page " + page + " of " + (totalCount / size) + ")");

                return new PagedResponse<>(methodDtos, page, size, (int) totalCount);

            } catch (Exception e) {
                System.err.println("Error fetching paginated test method details: " + e.getMessage());
                e.printStackTrace();
                return new PagedResponse<>(List.of(), page, size, 0);
            }
        } else {
            System.err.println("PersistenceReadFacade is not available - database may not be configured");
            return new PagedResponse<>(List.of(), page, size, 0);
        }
    }

    /**
     * Get hierarchical data for progressive loading
     * Supports drill-down: Team → Package → Class
     */
    public List<Map<String, Object>> getHierarchy(String level, String teamName, String packageName) {
        if (persistenceReadFacade.isPresent()) {
            try {
                // Get latest scan session IDs for ALL repositories
                Map<Long, Long> latestSessions = getLatestScanSessionIds();
                if (latestSessions.isEmpty()) {
                    System.err.println("No scan sessions found for any repository");
                    return List.of();
                }

                // Return aggregated data based on hierarchy level
                if ("TEAM".equalsIgnoreCase(level)) {
                    return persistenceReadFacade.get().getHierarchyByTeam(latestSessions);
                } else if ("PACKAGE".equalsIgnoreCase(level) && teamName != null) {
                    return persistenceReadFacade.get().getHierarchyByPackage(latestSessions, teamName);
                } else if ("CLASS".equalsIgnoreCase(level) && teamName != null && packageName != null) {
                    return persistenceReadFacade.get().getHierarchyByClass(latestSessions, teamName, packageName);
                }

                return List.of();

            } catch (Exception e) {
                System.err.println("Error fetching hierarchy: " + e.getMessage());
                e.printStackTrace();
                return List.of();
            }
        }
        return List.of();
    }

    /**
     * Helper method to get the latest scan session ID for each active repository
     */
    private Map<Long, Long> getLatestScanSessionIds() {
        if (persistenceReadFacade.isEmpty()) {
            return Map.of();
        }
        try {
            List<RepositoryRecord> repositories = persistenceReadFacade.get().listAllRepositories();
            Map<Long, Long> sessionIds = new HashMap<>();
            for (RepositoryRecord repo : repositories) {
                Optional<Long> sessionId = persistenceReadFacade.get()
                        .getLatestScanSessionIdForRepository(repo.getId());
                sessionId.ifPresent(id -> sessionIds.put(repo.getId(), id));
            }
            return sessionIds;
        } catch (Exception e) {
            System.err.println("Error fetching latest scan session IDs: " + e.getMessage());
            return Map.of();
        }
    }
}
