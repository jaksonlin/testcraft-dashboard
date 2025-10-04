package com.example.annotationextractor.web.controller;

import com.example.annotationextractor.application.PersistenceReadFacade;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.*;

/**
 * Debug controller to check database contents and connection
 */
@RestController
@RequestMapping("/debug")
@CrossOrigin(origins = "http://localhost:3000")
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

    /**
     * Raw database query to check tables
     */
    @GetMapping("/raw-query")
    public Map<String, Object> getRawQuery(@RequestParam(defaultValue = "SELECT COUNT(*) FROM repositories") String query) {
        Map<String, Object> result = new HashMap<>();
        
        if (persistenceReadFacade.isPresent()) {
            try {
                // This is a hack - we'll need to access the DataSource directly
                result.put("status", "facade_available");
                result.put("message", "Raw queries not implemented yet - facade only provides high-level methods");
                result.put("query", query);
            } catch (Exception e) {
                result.put("status", "error");
                result.put("error", e.getMessage());
            }
        } else {
            result.put("status", "no_connection");
        }
        
        return result;
    }
}
