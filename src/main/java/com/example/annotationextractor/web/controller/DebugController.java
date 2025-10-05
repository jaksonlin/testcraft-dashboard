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
}
