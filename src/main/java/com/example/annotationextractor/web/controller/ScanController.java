package com.example.annotationextractor.web.controller;

import com.example.annotationextractor.service.ScheduledScanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for scan operations
 * Provides endpoints for manual scan triggering and status
 */
@RestController
@RequestMapping("/scan")
@CrossOrigin(origins = "http://localhost:3000") // React dev server
public class ScanController {

    private final ScheduledScanService scheduledScanService;

    @Autowired
    public ScanController(ScheduledScanService scheduledScanService) {
        this.scheduledScanService = scheduledScanService;
    }

    /**
     * Trigger a manual scan
     */
    @PostMapping("/trigger")
    public ResponseEntity<Map<String, Object>> triggerScan() {
        Map<String, Object> response = new HashMap<>();
        
        try {
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
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getScanConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("tempCloneMode", false); // TODO: Get from configuration
        config.put("repositoryHubPath", "./repositories"); // TODO: Get from configuration
        config.put("lastScanTime", null); // TODO: Get from database
        
        return ResponseEntity.ok(config);
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
        
        return ResponseEntity.ok(health);
    }
}
