package com.example.annotationextractor.web.controller;

import com.example.annotationextractor.service.ScheduledScanService;
import com.example.annotationextractor.application.PersistenceReadFacade;
import com.example.annotationextractor.domain.model.ScanSession;
import com.example.annotationextractor.web.dto.ScanConfigDto;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}) // React dev server and Vite dev server
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
        response.put("scanBranch", status.getScanBranch());
        response.put("organization", status.getOrganization());
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
        config.put("maxRepositoriesPerScan", status.getMaxRepositoriesPerScan());
        config.put("schedulerEnabled", status.isSchedulerEnabled());
        config.put("dailyScanCron", status.getDailyScanCron());
        config.put("repositoryConfigContent", status.getRepositoryConfigContent());
        config.put("organization", status.getOrganization());
        config.put("scanBranch", status.getScanBranch());
        config.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(config);
    }

    /**
     * Update scan configuration
     */
    @PutMapping("/config")
    public ResponseEntity<Map<String, Object>> updateScanConfig(@RequestBody ScanConfigDto configDto) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate configuration
            if (configDto.getRepositoryHubPath() != null && configDto.getRepositoryHubPath().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Repository hub path cannot be empty");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (configDto.getRepositoryListFile() != null && configDto.getRepositoryListFile().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Repository list file cannot be empty");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (configDto.getMaxRepositoriesPerScan() != null && configDto.getMaxRepositoriesPerScan() <= 0) {
                response.put("success", false);
                response.put("message", "Max repositories per scan must be greater than 0");
                return ResponseEntity.badRequest().body(response);
            }

            if (configDto.getScanBranch() != null && configDto.getScanBranch().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Scan branch cannot be empty");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Update configuration
            boolean updated = scheduledScanService.updateScanConfiguration(configDto);
            
            if (updated) {
                response.put("success", true);
                response.put("message", "Scan configuration updated successfully");
                response.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to update scan configuration");
                return ResponseEntity.status(500).body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating scan configuration: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body(response);
        }
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

    /**
     * Download the Excel report for a specific scan session
     * This downloads the report file generated after the scan was completed
     */
    @GetMapping("/{scanId}/report")
    public ResponseEntity<Resource> downloadScanReport(@PathVariable Long scanId) {
        try {
            // Get scan session details
            if (!persistenceReadFacade.isPresent()) {
                return ResponseEntity.status(500).build();
            }
            
            Optional<ScanSession> scanSession = persistenceReadFacade.get().recentScanSessions(100)
                .stream()
                .filter(s -> s.getId().equals(scanId))
                .findFirst();
            
            if (scanSession.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            ScanSession session = scanSession.get();
            
            // Use the report file path stored in the database
            String reportFilePath = session.getReportFilePath();
            File reportFile = null;
            
            if (reportFilePath != null && !reportFilePath.isEmpty()) {
                // Use the stored absolute path
                reportFile = new File(reportFilePath);
            } else {
                // Fallback: Find the most recent report file
                // Reports are named: weekly_report_yyyyMMdd_HHmmss.xlsx
                Path reportsDir = Paths.get("reports");
                if (!Files.exists(reportsDir)) {
                    return ResponseEntity.notFound().build();
                }
                
                File[] reportFiles = reportsDir.toFile().listFiles((dir, name) -> 
                    name.startsWith("weekly_report_") && name.endsWith(".xlsx")
                );
                
                if (reportFiles != null && reportFiles.length > 0) {
                    // Get the most recent report
                    long latestTime = 0;
                    for (File file : reportFiles) {
                        if (file.lastModified() > latestTime) {
                            latestTime = file.lastModified();
                            reportFile = file;
                        }
                    }
                }
            }
            
            if (reportFile == null || !reportFile.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(reportFile);
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + reportFile.getName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(reportFile.length())
                .body(resource);
                
        } catch (Exception e) {
            System.err.println("Error downloading scan report: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}
