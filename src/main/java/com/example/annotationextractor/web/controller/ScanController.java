package com.example.annotationextractor.web.controller;

import com.example.annotationextractor.service.ScheduledScanService;
import com.example.annotationextractor.application.PersistenceReadFacade;
import com.example.annotationextractor.domain.model.ScanSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for scan operations
 * Provides endpoints for manual scan triggering and status
 */
@RestController
@RequestMapping("/scan")
@CrossOrigin(origins = "http://localhost:3000") // React dev server
public class ScanController {

    private final ScheduledScanService scheduledScanService;
    private final Optional<PersistenceReadFacade> persistenceReadFacade;

    public ScanController(ScheduledScanService scheduledScanService, Optional<PersistenceReadFacade> persistenceReadFacade) {
        this.scheduledScanService = scheduledScanService;
        this.persistenceReadFacade = persistenceReadFacade;
    }

    /**
     * Trigger a manual scan
     */
    @PostMapping("/trigger")
    public ResponseEntity<Map<String, Object>> triggerScan() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check if scan is already running
            ScheduledScanService.ScanStatus status = scheduledScanService.getScanStatus();
            if (status.isScanning()) {
                response.put("success", false);
                response.put("message", "Scan is already in progress");
                response.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.status(409).body(response); // Conflict
            }
            
            boolean success = scheduledScanService.triggerManualScan();
            
            response.put("success", success);
            response.put("message", success ? "Scan completed successfully" : "Scan failed");
            response.put("timestamp", System.currentTimeMillis());
            
            if (success) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(500).body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Scan failed with error: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get scan configuration and status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getScanStatus() {
        ScheduledScanService.ScanStatus status = scheduledScanService.getScanStatus();
        Map<String, Object> response = new HashMap<>();
        
        response.put("isScanning", status.isScanning());
        response.put("lastScanTime", status.getLastScanTime() != null ? 
            status.getLastScanTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
        response.put("lastScanStatus", status.getLastScanStatus());
        response.put("lastScanError", status.getLastScanError());
        response.put("repositoryHubPath", status.getRepositoryHubPath());
        response.put("repositoryListFile", status.getRepositoryListFile());
        response.put("tempCloneMode", status.isTempCloneMode());
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get scan configuration only
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getScanConfig() {
        ScheduledScanService.ScanStatus status = scheduledScanService.getScanStatus();
        Map<String, Object> config = new HashMap<>();
        
        config.put("tempCloneMode", status.isTempCloneMode());
        config.put("repositoryHubPath", status.getRepositoryHubPath());
        config.put("repositoryListFile", status.getRepositoryListFile());
        config.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(config);
    }

    /**
     * Get recent scan sessions from database
     */
    @GetMapping("/sessions")
    public ResponseEntity<List<ScanSession>> getRecentScanSessions(@RequestParam(defaultValue = "10") int limit) {
        if (persistenceReadFacade.isPresent()) {
            try {
                List<ScanSession> sessions = persistenceReadFacade.get().recentScanSessions(limit);
                return ResponseEntity.ok(sessions);
            } catch (Exception e) {
                return ResponseEntity.status(500).build();
            }
        }
        return ResponseEntity.ok(List.of());
    }

    /**
     * Health check for scan service
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getScanHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "healthy");
        health.put("service", "scan-service");
        health.put("timestamp", System.currentTimeMillis());
        
        // Add database connectivity status
        health.put("databaseAvailable", persistenceReadFacade.isPresent());
        
        return ResponseEntity.ok(health);
    }
}
