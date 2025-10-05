package com.example.annotationextractor.service;

import com.example.annotationextractor.application.PersistenceReadFacade;
import com.example.annotationextractor.domain.model.*;
import com.example.annotationextractor.web.dto.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 * Service for transforming domain models to dashboard DTOs
 */
@Service
public class DashboardDataService {

    private final Optional<PersistenceReadFacade> persistenceReadFacade;
    private final TeamDataService teamDataService;
    private final RepositoryDataService repositoryDataService;

    public DashboardDataService(Optional<PersistenceReadFacade> persistenceReadFacade, TeamDataService teamDataService, RepositoryDataService repositoryDataService) {
        this.persistenceReadFacade = persistenceReadFacade;
        this.teamDataService = teamDataService;
        this.repositoryDataService = repositoryDataService;
    }

    /**
     * Get dashboard overview data
     */
    public DashboardOverviewDto getDashboardOverview() {
        DashboardOverviewDto overview = new DashboardOverviewDto();
        
        if (persistenceReadFacade.isPresent()) {
            try {
                // Get all repositories
                List<RepositoryRecord> repositories = persistenceReadFacade.get().listAllRepositories();
                List<Team> teams = persistenceReadFacade.get().listTeams();
                List<ScanSession> recentSessions = persistenceReadFacade.get().recentScanSessions(1);
            
            // Calculate totals
            int totalRepositories = repositories.size();
            int totalTeams = teams.size();
            int totalTestClasses = repositories.stream().mapToInt(RepositoryRecord::getTotalTestClasses).sum();
            int totalTestMethods = repositories.stream().mapToInt(RepositoryRecord::getTotalTestMethods).sum();
            int totalAnnotatedMethods = repositories.stream().mapToInt(RepositoryRecord::getTotalAnnotatedMethods).sum();
            
            // Calculate overall coverage rate
            double overallCoverageRate = totalTestMethods > 0 ? 
                (double) totalAnnotatedMethods / totalTestMethods * 100 : 0.0;
            
            // Set overview data
            overview.setTotalRepositories(totalRepositories);
            overview.setTotalTeams(totalTeams);
            overview.setTotalTestClasses(totalTestClasses);
            overview.setTotalTestMethods(totalTestMethods);
            overview.setTotalAnnotatedMethods(totalAnnotatedMethods);
            overview.setOverallCoverageRate(overallCoverageRate);
            
            // Set last scan date
            if (!recentSessions.isEmpty()) {
                overview.setLastScanDate(recentSessions.get(0).getScanDate()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime());
            }
            
            // Get top teams (by repository count)
            overview.setTopTeams(teamDataService.getTopTeams(teams, repositories, 5));
            
                // Get top repositories (by coverage rate)
                overview.setTopRepositories(repositoryDataService.getTopRepositories(repositories, teams, 5));
                
            } catch (Exception e) {
                // If database is not available, return empty data
                overview.setTotalRepositories(0);
                overview.setTotalTeams(0);
                overview.setTotalTestClasses(0);
                overview.setTotalTestMethods(0);
                overview.setTotalAnnotatedMethods(0);
                overview.setOverallCoverageRate(0.0);
            }
        } else {
            // Return mock data when database integration is disabled
            overview.setTotalRepositories(3);
            overview.setTotalTeams(2);
            overview.setTotalTestClasses(15);
            overview.setTotalTestMethods(127);
            overview.setTotalAnnotatedMethods(45);
            overview.setOverallCoverageRate(35.43);
        }
        
        return overview;
    }

    
    

    /**
     * Get coverage trends for the specified period
     */
    public List<DailyMetric> getCoverageTrends(int days) {
        if (persistenceReadFacade.isPresent()) {
            try {
                LocalDate endDate = LocalDate.now();
                LocalDate startDate = endDate.minusDays(days);
                return persistenceReadFacade.get().dailyMetricsRange(startDate, endDate);
            } catch (Exception e) {
                return List.of();
            }
        } else {
            // Return empty list for mock data
            return List.of();
        }
    }

    /**
     * Get recent scan sessions
     */
    public List<ScanSession> getRecentScanSessions(int limit) {
        if (persistenceReadFacade.isPresent()) {
            try {
                return persistenceReadFacade.get().recentScanSessions(limit);
            } catch (Exception e) {
                return List.of();
            }
        } else {
            // Return empty list for mock data
            return List.of();
        }
    }

    

   

    

    
}
