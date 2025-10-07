package com.example.annotationextractor.web.controller;

import com.example.annotationextractor.application.PersistenceReadFacade;
import com.example.annotationextractor.domain.model.RepositoryRecord;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.*;

/**
 * Debug controller to check database contents and connection
 */
@RestController
@RequestMapping("/debug")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class DebugController {

    private final Optional<PersistenceReadFacade> persistenceReadFacade;

    public DebugController(Optional<PersistenceReadFacade> persistenceReadFacade) {
        this.persistenceReadFacade = persistenceReadFacade;
    }

    /**
     * Check database connection and basic info
     */
    @GetMapping("/database-info")
    public Map<String, Object> getDatabaseInfo() {
        Map<String, Object> info = new HashMap<>();
        
        if (persistenceReadFacade.isPresent()) {
            info.put("persistenceFacadeAvailable", true);
            info.put("status", "Connected to database");
        } else {
            info.put("persistenceFacadeAvailable", false);
            info.put("status", "No database connection");
        }
        
        return info;
    }

    /**
     * Check table counts
     */
    @GetMapping("/table-counts")
    public Map<String, Object> getTableCounts() {
        Map<String, Object> counts = new HashMap<>();
        
        if (persistenceReadFacade.isPresent()) {
            try {
                PersistenceReadFacade facade = persistenceReadFacade.get();
                
                // Try to get basic counts
                List<?> repositories = facade.listAllRepositories();
                List<?> teams = facade.listTeams();
                List<?> recentSessions = facade.recentScanSessions(1);
                
                counts.put("repositories", repositories.size());
                counts.put("teams", teams.size());
                counts.put("recentScanSessions", recentSessions.size());
                counts.put("status", "success");
                
            } catch (Exception e) {
                counts.put("status", "error");
                counts.put("error", e.getMessage());
            }
        } else {
            counts.put("status", "no_connection");
        }
        
        return counts;
    }

    @GetMapping("/repositories-raw")
    public ResponseEntity<List<RepositoryRecord>> getRawRepositories() {
        if (persistenceReadFacade.isPresent()) {
            try {
                return ResponseEntity.ok(persistenceReadFacade.get().listAllRepositories());
            } catch (Exception e) {
                return ResponseEntity.status(500).build();
            }
        }
        return ResponseEntity.ok(List.of());
    }

    /**
     * Debug endpoint to check test methods for a repository without scan session filtering
     */
    @GetMapping("/repository/{repositoryId}/test-methods-raw")
    public Map<String, Object> getRawTestMethods(@PathVariable Long repositoryId) {
        Map<String, Object> result = new HashMap<>();
        
        if (persistenceReadFacade.isPresent()) {
            try {
                PersistenceReadFacade facade = persistenceReadFacade.get();
                
                // Get latest scan session
                Optional<?> latestScan = facade.getLatestCompletedScanSession();
                if (latestScan.isPresent()) {
                    result.put("latestScanSessionId", latestScan.get().toString());
                } else {
                    result.put("latestScanSessionId", "NOT_FOUND");
                }
                
                // Get test classes for repository
                List<?> classes = facade.listClassesByRepositoryIdAndScanSessionId(repositoryId, 11L); // Use scan session 11
                result.put("testClasses", classes.size());
                
                // Get test methods for repository (without scan session filter)
                List<?> allMethods = facade.listTestMethodDetailsByRepositoryIdAndScanSessionId(repositoryId, 11L, 1000);
                result.put("testMethods", allMethods.size());
                
                // Check different scan session IDs
                Map<String, Integer> scanSessionCounts = new HashMap<>();
                for (long sessionId = 1; sessionId <= 11; sessionId++) {
                    try {
                        List<?> methods = facade.listTestMethodDetailsByRepositoryIdAndScanSessionId(repositoryId, sessionId, 1000);
                        scanSessionCounts.put("session_" + sessionId, methods.size());
                    } catch (Exception e) {
                        scanSessionCounts.put("session_" + sessionId, -1);
                    }
                }
                result.put("scanSessionCounts", scanSessionCounts);
                
                result.put("status", "success");
                
            } catch (Exception e) {
                result.put("status", "error");
                result.put("error", e.getMessage());
                e.printStackTrace();
            }
        } else {
            result.put("status", "no_connection");
        }
        
        return result;
    }
}
