package com.example.annotationextractor.service;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Comparator;

import org.springframework.stereotype.Service;

import com.example.annotationextractor.application.PersistenceReadFacade;
import com.example.annotationextractor.domain.model.RepositoryRecord;
import com.example.annotationextractor.domain.model.Team;
import com.example.annotationextractor.web.dto.RepositorySummaryDto;
import com.example.annotationextractor.web.dto.TeamMetricsDto;
import com.example.annotationextractor.web.dto.TeamSummaryDto;
import com.example.annotationextractor.web.dto.PagedResponse;

@Service
public class TeamDataService {
    // Helper methods for data transformation
    private final Optional<PersistenceReadFacade> persistenceReadFacade;
    public TeamDataService(Optional<PersistenceReadFacade> persistenceReadFacade) {
        this.persistenceReadFacade = persistenceReadFacade;
    }

    /**
     * Get team metrics
     */
    public List<TeamMetricsDto> getTeamMetrics() {
        if (persistenceReadFacade.isPresent()) {
            try {
                List<Team> teams = persistenceReadFacade.get().listTeams();
                List<RepositoryRecord> repositories = persistenceReadFacade.get().listAllRepositories();
                
                return teams.stream()
                    .map(team -> convertToTeamMetricsDto(team, repositories))
                    .collect(Collectors.toList());
                    
            } catch (Exception e) {
                return List.of();
            }
        } 
        
        return List.of();
    }

    /**
     * Get teams with pagination and filtering
     */
    public PagedResponse<TeamMetricsDto> getTeamsPaginated(
            int page, int size, String search, String sortBy, String sortOrder) {
        if (persistenceReadFacade.isPresent()) {
            try {
                List<Team> teams = persistenceReadFacade.get().listTeams();
                List<RepositoryRecord> repositories = persistenceReadFacade.get().listAllRepositories();
                
                // Convert to DTOs
                List<TeamMetricsDto> teamMetrics = teams.stream()
                    .map(team -> convertToTeamMetricsDto(team, repositories))
                    .collect(Collectors.toList());
                
                // Apply search filter
                if (search != null && !search.trim().isEmpty()) {
                    String searchLower = search.toLowerCase();
                    teamMetrics = teamMetrics.stream()
                        .filter(team -> 
                            team.getTeamName().toLowerCase().contains(searchLower) ||
                            team.getTeamCode().toLowerCase().contains(searchLower) ||
                            (team.getDepartment() != null && team.getDepartment().toLowerCase().contains(searchLower))
                        )
                        .collect(Collectors.toList());
                }
                
                // Apply sorting
                if (sortBy != null && !sortBy.trim().isEmpty()) {
                    Comparator<TeamMetricsDto> comparator = getComparator(sortBy);
                    if ("desc".equalsIgnoreCase(sortOrder)) {
                        comparator = comparator.reversed();
                    }
                    teamMetrics.sort(comparator);
                }
                
                // Apply pagination
                int totalElements = teamMetrics.size();
                int totalPages = (int) Math.ceil((double) totalElements / size);
                int startIndex = page * size;
                int endIndex = Math.min(startIndex + size, totalElements);
                
                List<TeamMetricsDto> pagedContent = startIndex < totalElements 
                    ? teamMetrics.subList(startIndex, endIndex)
                    : List.of();
                
                return new PagedResponse<>(pagedContent, page, size, totalElements);
                
            } catch (Exception e) {
                System.err.println("Error fetching paginated teams: " + e.getMessage());
                return new PagedResponse<>(List.of(), page, size, 0);
            }
        }
        
        return new PagedResponse<>(List.of(), page, size, 0);
    }

    private Comparator<TeamMetricsDto> getComparator(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "name" -> Comparator.comparing(TeamMetricsDto::getTeamName);
            case "repositories" -> Comparator.comparing(TeamMetricsDto::getRepositoryCount);
            case "coverage" -> Comparator.comparing(TeamMetricsDto::getAverageCoverageRate);
            default -> Comparator.comparing(TeamMetricsDto::getTeamName);
        };
    }


    public List<TeamSummaryDto> getTopTeams(List<Team> teams, List<RepositoryRecord> repositories, int limit) {
        return teams.stream()
            .map(team -> {
                TeamSummaryDto summary = new TeamSummaryDto(team.getId(), team.getTeamName(), team.getTeamCode());
                List<RepositoryRecord> teamRepos = repositories.stream()
                    .filter(repo -> team.getId().equals(repo.getTeamId()))
                    .collect(Collectors.toList());
                
                summary.setRepositoryCount(teamRepos.size());
                summary.setTotalTestMethods(teamRepos.stream().mapToInt(RepositoryRecord::getTotalTestMethods).sum());
                summary.setTotalAnnotatedMethods(teamRepos.stream().mapToInt(RepositoryRecord::getTotalAnnotatedMethods).sum());
                
                double avgCoverage = teamRepos.stream()
                    .mapToDouble(RepositoryRecord::getAnnotationCoverageRate)
                    .average()
                    .orElse(0.0);
                summary.setAverageCoverageRate(avgCoverage);
                
                return summary;
            })
            .sorted((a, b) -> Integer.compare(b.getRepositoryCount(), a.getRepositoryCount()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    

    private TeamMetricsDto convertToTeamMetricsDto(Team team, List<RepositoryRecord> repositories) {
        TeamMetricsDto dto = new TeamMetricsDto(team.getId(), team.getTeamName(), team.getTeamCode());
        dto.setDepartment(team.getDepartment());
        
        List<RepositoryRecord> teamRepos = repositories.stream()
            .filter(repo -> team.getId().equals(repo.getTeamId()))
            .collect(Collectors.toList());
        
        dto.setRepositoryCount(teamRepos.size());
        dto.setTotalTestClasses(teamRepos.stream().mapToInt(RepositoryRecord::getTotalTestClasses).sum());
        dto.setTotalTestMethods(teamRepos.stream().mapToInt(RepositoryRecord::getTotalTestMethods).sum());
        dto.setTotalAnnotatedMethods(teamRepos.stream().mapToInt(RepositoryRecord::getTotalAnnotatedMethods).sum());
        
        double avgCoverage = teamRepos.stream()
            .mapToDouble(RepositoryRecord::getAnnotationCoverageRate)
            .average()
            .orElse(0.0);
        dto.setAverageCoverageRate(avgCoverage);
        
        // Set repositories
        List<RepositorySummaryDto> repoSummaries = teamRepos.stream()
            .map(repo -> convertToRepositorySummaryDto(repo, List.of(team)))
            .collect(Collectors.toList());
        dto.setRepositories(repoSummaries);
        
        return dto;
    }

    

    private RepositorySummaryDto convertToRepositorySummaryDto(RepositoryRecord repo, List<Team> teams) {
        RepositorySummaryDto dto = new RepositorySummaryDto(repo.getId(), repo.getRepositoryName(), repo.getGitUrl());
        
        String teamName = teams.stream()
            .filter(team -> team.getId().equals(repo.getTeamId()))
            .findFirst()
            .map(Team::getTeamName)
            .orElse("Unknown");
        
        dto.setTeamName(teamName);
        dto.setTestClassCount(repo.getTotalTestClasses());
        dto.setTestMethodCount(repo.getTotalTestMethods());
        dto.setAnnotatedMethodCount(repo.getTotalAnnotatedMethods());
        dto.setCoverageRate(repo.getAnnotationCoverageRate());
        dto.setLastScanDate(repo.getLastScanDate()
            .atZone(ZoneId.systemDefault()).toLocalDateTime());
        
        return dto;
    }

   
}
