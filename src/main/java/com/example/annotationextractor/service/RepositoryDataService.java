package com.example.annotationextractor.service;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.annotationextractor.application.PersistenceReadFacade;
import com.example.annotationextractor.domain.model.RepositoryDetailRecord;
import com.example.annotationextractor.domain.model.RepositoryRecord;
import com.example.annotationextractor.domain.model.Team;
import com.example.annotationextractor.domain.model.TestMethodDetailRecord;
import com.example.annotationextractor.domain.model.TestClass;
import com.example.annotationextractor.domain.model.ScanSession;
import com.example.annotationextractor.web.dto.RepositoryDetailDto;
import com.example.annotationextractor.web.dto.RepositoryMetricsDto;
import com.example.annotationextractor.web.dto.RepositorySummaryDto;
import com.example.annotationextractor.web.dto.TestMethodDetailDto;
import com.example.annotationextractor.web.dto.TestClassSummaryDto;

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

    public List<TestMethodDetailDto> getTestMethodDetailsByTeamId(Long teamId, Integer limit) {
        Optional<ScanSession> latestScan = persistenceReadFacade.get().getLatestCompletedScanSession();
        if (latestScan.isEmpty()) {
            System.err.println("No completed scan session found");
            return List.of();
        }
        Long scanSessionId = latestScan.get().getId();
        List<TestMethodDetailRecord> records = persistenceReadFacade.get().listTestMethodDetailsByTeamIdAndScanSessionId(teamId, scanSessionId, limit);
        return records.stream()
            .map(this::convertToTestMethodDetailDto)
            .collect(Collectors.toList());
    }
}
