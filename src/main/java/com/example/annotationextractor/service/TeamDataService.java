package com.example.annotationextractor.service;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


import org.springframework.stereotype.Service;

import com.example.annotationextractor.application.PersistenceReadFacade;
import com.example.annotationextractor.domain.model.RepositoryRecord;
import com.example.annotationextractor.domain.model.Team;
import com.example.annotationextractor.web.dto.RepositorySummaryDto;
import com.example.annotationextractor.web.dto.TeamMetricsDto;
import com.example.annotationextractor.web.dto.TeamSummaryDto;

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
