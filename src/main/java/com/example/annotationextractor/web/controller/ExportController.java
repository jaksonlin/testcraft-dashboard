package com.example.annotationextractor.web.controller;

import com.example.annotationextractor.service.ExportService;
import com.example.annotationextractor.web.dto.ExportRequestDto;
import com.example.annotationextractor.web.dto.ExportStatusDto;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for handling data exports
 * Implements server-side export for better performance with large datasets
 */
@RestController
@RequestMapping("/export")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class ExportController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    /**
     * Test endpoint to verify controller is working
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("ExportController is working!");
    }

    /**
     * Initiate a new export job
     * Returns a job ID for tracking progress
     */
    @PostMapping("/initiate")
    public ResponseEntity<ExportStatusDto> initiateExport(@RequestBody ExportRequestDto request) {
        try {
            String jobId = UUID.randomUUID().toString();
            ExportStatusDto status = exportService.initiateExport(jobId, request);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            System.err.println("Error initiating export: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Check the status of an export job
     */
    @GetMapping("/status/{jobId}")
    public ResponseEntity<ExportStatusDto> getExportStatus(@PathVariable String jobId) {
        try {
            ExportStatusDto status = exportService.getExportStatus(jobId);
            if (status != null) {
                return ResponseEntity.ok(status);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Error getting export status: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Download the completed export file
     */
    @GetMapping("/download/{jobId}")
    public ResponseEntity<Resource> downloadExport(@PathVariable String jobId) {
        try {
            Resource file = exportService.getExportFile(jobId);
            if (file != null && file.exists()) {
                String filename = exportService.getExportFilename(jobId);
                
                return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(file);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Error downloading export: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Cancel an ongoing export job
     */
    @DeleteMapping("/cancel/{jobId}")
    public ResponseEntity<Void> cancelExport(@PathVariable String jobId) {
        try {
            boolean cancelled = exportService.cancelExport(jobId);
            if (cancelled) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Error cancelling export: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Clean up old export files
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<Void> cleanupOldExports() {
        try {
            exportService.cleanupOldExports();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.err.println("Error cleaning up exports: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
