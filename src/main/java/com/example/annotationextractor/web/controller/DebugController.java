package com.example.annotationextractor.web.controller;

import com.example.annotationextractor.application.PersistenceReadFacade;
import com.example.annotationextractor.domain.model.RepositoryRecord;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.annotationextractor.domain.model.ScanSession;
import java.sql.*;
import java.util.*;

/**
 * Debug controller to check database contents and connection
 */
@RestController
@RequestMapping("/debug")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:5173" })
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
     * Debug endpoint to check test methods for a repository without scan session
     * filtering
     */
    /**
     * Debug endpoint to check test methods for a repository without scan session
     * filtering
     */
    @GetMapping("/repository/{repositoryId}/test-methods-raw")
    public Map<String, Object> getRawTestMethods(@PathVariable Long repositoryId) {
        Map<String, Object> result = new HashMap<>();

        if (persistenceReadFacade.isPresent()) {
            try {
                PersistenceReadFacade facade = persistenceReadFacade.get();

                // Get latest scan session
                Optional<ScanSession> latestScan = facade.getLatestCompletedScanSession();
                Long scanSessionId;

                if (latestScan.isPresent()) {
                    scanSessionId = latestScan.get().getId();
                    result.put("latestScanSessionId", scanSessionId);
                    result.put("latestScanDate", latestScan.get().getScanDate());
                } else {
                    // Fallback to most recent session if no completed one exists
                    List<ScanSession> recent = facade.recentScanSessions(1);
                    if (!recent.isEmpty()) {
                        scanSessionId = recent.get(0).getId();
                        result.put("latestScanSessionId", scanSessionId + " (Incomplete/Latest)");
                    } else {
                        result.put("latestScanSessionId", "NOT_FOUND");
                        result.put("error", "No scan sessions found");
                        return result;
                    }
                }

                // Get test classes for repository
                List<com.example.annotationextractor.domain.model.TestClass> classes = facade
                        .listClassesByRepositoryIdAndScanSessionId(repositoryId, scanSessionId);
                result.put("testClasses", classes.size());

                // Get test methods for repository
                List<com.example.annotationextractor.domain.model.TestMethodDetailRecord> allMethods = facade
                        .listTestMethodDetailsByRepositoryIdAndScanSessionId(repositoryId, scanSessionId, 1000);
                result.put("testMethods", allMethods.size());

                // Check recent scan sessions for comparison
                Map<String, Integer> scanSessionCounts = new HashMap<>();
                List<ScanSession> recentSessions = facade.recentScanSessions(5);

                for (ScanSession session : recentSessions) {
                    try {
                        long count = facade.countTestMethodDetailsByRepositoryIdAndScanSessionId(repositoryId,
                                session.getId());
                        scanSessionCounts.put("session_" + session.getId(), (int) count);
                    } catch (Exception e) {
                        scanSessionCounts.put("session_" + session.getId(), -1);
                    }
                }
                result.put("recentSessionCounts", scanSessionCounts);

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
