package com.example.annotationextractor.service;

import java.time.ZoneId;
import java.util.ArrayList;
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
import com.example.annotationextractor.domain.model.ScanSession;
import com.example.annotationextractor.web.dto.PagedResponse;
import com.example.annotationextractor.web.dto.RepositoryDetailDto;
import com.example.annotationextractor.web.dto.RepositoryMetricsDto;
import com.example.annotationextractor.web.dto.RepositorySummaryDto;
import com.example.annotationextractor.web.dto.TestMethodDetailDto;
import com.example.annotationextractor.web.dto.TestClassSummaryDto;
import com.example.annotationextractor.web.dto.GroupedTestMethodResponse;

@Service
public class RepositoryDataService {

    private final Optional<PersistenceReadFacade> persistenceReadFacade;

    public RepositoryDataService(Optional<PersistenceReadFacade> persistenceReadFacade) {
        this.persistenceReadFacade = persistenceReadFacade;
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
                            if (!matchesLastScanRange(repo.getLastScanDate() != null ? repo.getLastScanDate().toString() : null, lastScan)) {
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
                // Get the latest completed scan session
                Optional<ScanSession> latestScan = persistenceReadFacade.get().getLatestCompletedScanSession();
                if (latestScan.isEmpty()) {
                    System.err.println("No completed scan session found");
                    return List.of();
                }
                
                Long scanSessionId = latestScan.get().getId();
                List<TestClass> classes = persistenceReadFacade.get().listClassesByRepositoryIdAndScanSessionId(repositoryId, scanSessionId);
                
                // Filter classes by scan session ID
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
    public PagedResponse<TestClassSummaryDto> getRepositoryClassesPaginated(Long repositoryId, int page, int size, String className, Boolean annotated) {
        if (persistenceReadFacade.isPresent()) {
            try {
                // Get the latest completed scan session
                Optional<ScanSession> latestScan = persistenceReadFacade.get().getLatestCompletedScanSession();
                if (latestScan.isEmpty()) {
                    System.err.println("No completed scan session found");
                    return new PagedResponse<TestClassSummaryDto>(List.of(), page, size, 0);
                }
                
                Long scanSessionId = latestScan.get().getId();
                List<TestClass> allClasses = persistenceReadFacade.get().listClassesByRepositoryIdAndScanSessionId(repositoryId, scanSessionId);
                
                // Filter classes by scan session ID
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
                                List<TestMethodDetailRecord> methods = persistenceReadFacade.get().listTestMethodDetailsByClassId(testClass.getId(), 1000);
                                boolean hasAnnotatedMethod = methods.stream().anyMatch(method -> 
                                    method.getAnnotationTitle() != null && !method.getAnnotationTitle().trim().isEmpty() && 
                                    !method.getAnnotationTitle().equals("No title"));
                                
                                if (annotated && !hasAnnotatedMethod) return false;
                                if (!annotated && hasAnnotatedMethod) return false;
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
                // Get all test methods for the repository, then filter by class and scan session
                List<TestMethodDetailRecord> records = persistenceReadFacade.get().listTestMethodDetailsByClassId(classId, limit);
                
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
        dto.setTeamName(record.getTeamName());
        dto.setTeamCode(record.getTeamCode());
        dto.setGitUrl(record.getGitUrl());
        return dto;
    }

    public List<RepositorySummaryDto> getTopRepositories(List<RepositoryRecord> repositories, List<Team> teams, int limit) {
        return repositories.stream()
            .map(repo -> {
                RepositorySummaryDto summary = new RepositorySummaryDto(repo.getId(), repo.getRepositoryName(), repo.getGitUrl());
                
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
                Optional<ScanSession> latestScan = persistenceReadFacade.get().getLatestCompletedScanSession();
                if (latestScan.isEmpty()) {
                    System.err.println("No completed scan session found");
                    return List.of();
                }
                Long scanSessionId = latestScan.get().getId();
                System.err.println("Using scan session ID: " + scanSessionId + " for all test methods");
                List<TestMethodDetailRecord> records = persistenceReadFacade.get().listTestMethodDetailsByScanSessionId(scanSessionId, limit);
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
                //Long scanSessionId = findLatestScanSessionWithDataForRepository(repositoryId);
                Optional<ScanSession> latestScan = persistenceReadFacade.get().getLatestCompletedScanSession();
                if (latestScan.isEmpty()) {
                    System.err.println("No scan session found with data for repository: " + repositoryId);
                    return List.of();
                }
                Long scanSessionId = latestScan.get().getId();
                System.err.println("Using scan session ID: " + scanSessionId + " for repository: " + repositoryId);
                List<TestMethodDetailRecord> records = persistenceReadFacade.get().listTestMethodDetailsByRepositoryIdAndScanSessionId(repositoryId, scanSessionId, limit);
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
                        System.err.println("Found test classes for repository " + repositoryId + " in scan session " + session.getId());
                        return session.getId();
                    }
                    
                    // Fallback: check for test methods
                    List<TestMethodDetailRecord> methods = persistenceReadFacade.get()
                        .listTestMethodDetailsByRepositoryIdAndScanSessionId(repositoryId, session.getId(), 1);
                    if (!methods.isEmpty()) {
                        System.err.println("Found test methods for repository " + repositoryId + " in scan session " + session.getId());
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
                Optional<ScanSession> latestScan = persistenceReadFacade.get().getLatestCompletedScanSession();
                if (latestScan.isEmpty()) {
                    System.err.println("No completed scan session found");
                    return List.of();
                }
                Long scanSessionId = latestScan.get().getId();
                System.err.println("Using scan session ID: " + scanSessionId + " for team: " + teamId);
                List<TestMethodDetailRecord> records = persistenceReadFacade.get().listTestMethodDetailsByTeamIdAndScanSessionId(teamId, scanSessionId, limit);
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
     * Get all test method details grouped by team and class for hierarchical display
     * This method provides pre-grouped data to avoid performance issues on the frontend
     */
    public GroupedTestMethodResponse getAllTestMethodDetailsGrouped(Integer limit) {
        if (persistenceReadFacade.isPresent()) {
            try {
                Optional<ScanSession> latestScan = persistenceReadFacade.get().getLatestCompletedScanSession();
                if (latestScan.isEmpty()) {
                    System.err.println("No completed scan session found");
                    return new GroupedTestMethodResponse(List.of(), 
                        new GroupedTestMethodResponse.SummaryDto(0, 0, 0, 0, 0.0));
                }
                
                Long scanSessionId = latestScan.get().getId();
                System.err.println("Using scan session ID: " + scanSessionId + " for grouped test methods");
                List<TestMethodDetailRecord> records = persistenceReadFacade.get().listTestMethodDetailsByScanSessionId(scanSessionId, limit);
                System.err.println("Found " + records.size() + " total test method records for grouping");
                
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
            .collect(Collectors.groupingBy(record -> record.getTeamName() != null ? record.getTeamName() : "Unknown Team"));
        
        List<GroupedTestMethodResponse.TeamGroupDto> teamDtos = new ArrayList<>();
        int totalTeams = teamGroups.size();
        int totalClasses = 0;
        int totalMethods = records.size();
        int totalAnnotatedMethods = 0;
        
        for (Map.Entry<String, List<TestMethodDetailRecord>> teamEntry : teamGroups.entrySet()) {
            String teamName = teamEntry.getKey();
            List<TestMethodDetailRecord> teamMethods = teamEntry.getValue();
            
            // Get team code from first record
            String teamCode = teamMethods.isEmpty() ? "" : 
                (teamMethods.get(0).getTeamCode() != null ? teamMethods.get(0).getTeamCode() : "");
            
            // Group by class within team
            Map<String, List<TestMethodDetailRecord>> classGroups = teamMethods.stream()
                .collect(Collectors.groupingBy(record -> 
                    (record.getRepositoryName() != null ? record.getRepositoryName() : "Unknown") + "." + 
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
                    .filter(record -> record.getAnnotationTitle() != null && !record.getAnnotationTitle().trim().isEmpty())
                    .count();
                double classCoverageRate = classTotalMethods > 0 ? 
                    (double) classAnnotatedMethods / classTotalMethods * 100 : 0.0;
                
                GroupedTestMethodResponse.ClassSummaryDto classSummary = 
                    new GroupedTestMethodResponse.ClassSummaryDto(classTotalMethods, classAnnotatedMethods, classCoverageRate);
                
                GroupedTestMethodResponse.ClassGroupDto classDto = 
                    new GroupedTestMethodResponse.ClassGroupDto(className, "", repository, methodDtos, classSummary);
                
                classDtos.add(classDto);
                teamAnnotatedMethods += classAnnotatedMethods;
            }
            
            // Calculate team summary
            double teamCoverageRate = teamTotalMethods > 0 ? 
                (double) teamAnnotatedMethods / teamTotalMethods * 100 : 0.0;
            
            GroupedTestMethodResponse.TeamSummaryDto teamSummary = 
                new GroupedTestMethodResponse.TeamSummaryDto(teamTotalClasses, teamTotalMethods, teamAnnotatedMethods, teamCoverageRate);
            
            GroupedTestMethodResponse.TeamGroupDto teamDto = 
                new GroupedTestMethodResponse.TeamGroupDto(teamName, teamCode, classDtos, teamSummary);
            
            teamDtos.add(teamDto);
            totalClasses += teamTotalClasses;
            totalAnnotatedMethods += teamAnnotatedMethods;
        }
        
        // Calculate overall summary
        double overallCoverageRate = totalMethods > 0 ? 
            (double) totalAnnotatedMethods / totalMethods * 100 : 0.0;
        
        GroupedTestMethodResponse.SummaryDto summary = 
            new GroupedTestMethodResponse.SummaryDto(totalTeams, totalClasses, totalMethods, totalAnnotatedMethods, overallCoverageRate);
        
        return new GroupedTestMethodResponse(teamDtos, summary);
    }

    /**
     * Get test method details with pagination and filtering for better performance
     * Note: This implementation uses client-side pagination due to current PersistenceReadFacade limitations
     */
    public PagedResponse<TestMethodDetailDto> getTestMethodDetailsPaginated(
            int page, int size, String teamName, String repositoryName, Boolean annotated) {
        
        if (persistenceReadFacade.isPresent()) {
            try {
                Optional<ScanSession> latestScan = persistenceReadFacade.get().getLatestCompletedScanSession();
                if (latestScan.isEmpty()) {
                    System.err.println("No completed scan session found");
                    return new PagedResponse<>(List.of(), page, size, 0);
                }
                
                Long scanSessionId = latestScan.get().getId();
                System.err.println("Using scan session ID: " + scanSessionId + " for paginated test methods");
                
                // Get all records first (this is a limitation of current implementation)
                // TODO: Implement proper database-level pagination in PersistenceReadFacade
                List<TestMethodDetailRecord> allRecords = persistenceReadFacade.get()
                    .listTestMethodDetailsByScanSessionId(scanSessionId, 10000); // Large limit for now
                
                // Apply filters
                List<TestMethodDetailRecord> filteredRecords = allRecords.stream()
                    .filter(record -> {
                        if (teamName != null && !teamName.trim().isEmpty()) {
                            if (record.getTeamName() == null || 
                                !record.getTeamName().toLowerCase().contains(teamName.toLowerCase())) {
                                return false;
                            }
                        }
                        if (repositoryName != null && !repositoryName.trim().isEmpty()) {
                            if (record.getRepositoryName() == null || 
                                !record.getRepositoryName().toLowerCase().contains(repositoryName.toLowerCase())) {
                                return false;
                            }
                        }
                        if (annotated != null) {
                            boolean isAnnotated = record.getAnnotationTitle() != null && 
                                                 !record.getAnnotationTitle().trim().isEmpty();
                            if (annotated != isAnnotated) {
                                return false;
                            }
                        }
                        return true;
                    })
                    .collect(Collectors.toList());
                
                // Apply pagination
                int totalCount = filteredRecords.size();
                int startIndex = page * size;
                int endIndex = Math.min(startIndex + size, totalCount);
                
                List<TestMethodDetailRecord> paginatedRecords = filteredRecords.subList(startIndex, endIndex);
                
                // Convert to DTOs
                List<TestMethodDetailDto> methodDtos = paginatedRecords.stream()
                    .map(this::convertToTestMethodDetailDto)
                    .collect(Collectors.toList());
                
                return new PagedResponse<>(methodDtos, page, size, totalCount);
                
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
}
