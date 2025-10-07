package com.example.annotationextractor.web.controller;

import com.example.annotationextractor.testcase.ExcelParserService;
import com.example.annotationextractor.testcase.TestCase;
import com.example.annotationextractor.testcase.TestCaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * REST controller for test case upload and management
 */
@RestController
@RequestMapping("/api/testcases")
@CrossOrigin(origins = "*")
public class TestCaseController {
    
    private final TestCaseService testCaseService;
    
    public TestCaseController(TestCaseService testCaseService) {
        this.testCaseService = testCaseService;
    }
    
    /**
     * Upload Excel file and get preview with auto-detected mappings
     * 
     * POST /api/testcases/upload/preview
     */
    @PostMapping("/upload/preview")
    public ResponseEntity<?> previewExcel(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
            }
            
            String filename = file.getOriginalFilename();
            if (!isExcelFile(filename)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Only Excel files (.xls, .xlsx) are supported"));
            }
            
            ExcelParserService.ExcelPreview preview = testCaseService.previewExcel(
                file.getInputStream(),
                filename
            );
            
            // Also run validation on suggested mappings
            ExcelParserService.ValidationResult validation = testCaseService.validateMappings(
                preview.getSuggestedMappings(),
                preview.getColumns()
            );
            
            return ResponseEntity.ok(Map.of(
                "columns", preview.getColumns(),
                "previewData", preview.getPreviewData(),
                "suggestedMappings", preview.getSuggestedMappings(),
                "confidence", preview.getConfidence(),
                "suggestedDataStartRow", preview.getSuggestedDataStartRow(),
                "validation", Map.of(
                    "valid", validation.isValid(),
                    "missingRequiredFields", validation.getMissingRequiredFields(),
                    "suggestions", validation.getSuggestions()
                )
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to preview Excel: " + e.getMessage()));
        }
    }
    
    /**
     * Validate column mappings
     * 
     * POST /api/testcases/upload/validate
     */
    @PostMapping("/upload/validate")
    public ResponseEntity<?> validateMappings(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, String> mappings = (Map<String, String>) request.get("mappings");
            
            @SuppressWarnings("unchecked")
            List<String> columns = (List<String>) request.get("columns");
            
            ExcelParserService.ValidationResult validation = testCaseService.validateMappings(mappings, columns);
            
            return ResponseEntity.ok(Map.of(
                "valid", validation.isValid(),
                "missingRequiredFields", validation.getMissingRequiredFields(),
                "suggestions", validation.getSuggestions()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Import test cases from Excel
     * 
     * POST /api/testcases/upload/import
     */
    @PostMapping("/upload/import")
    public ResponseEntity<?> importTestCases(
            @RequestParam("file") MultipartFile file,
            @RequestParam("mappings") String mappingsJson,
            @RequestParam("dataStartRow") int dataStartRow,
            @RequestParam(value = "replaceExisting", defaultValue = "true") boolean replaceExisting,
            @RequestParam(value = "createdBy", defaultValue = "system") String createdBy,
            @RequestParam(value = "organization", defaultValue = "default") String organization) {
        
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
            }
            
            // Parse mappings JSON
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, String> columnMappings = mapper.readValue(mappingsJson, Map.class);
            
            // Import
            TestCaseService.ImportResult result = testCaseService.importTestCases(
                file.getInputStream(),
                file.getOriginalFilename(),
                columnMappings,
                dataStartRow,
                replaceExisting,
                createdBy,
                organization
            );
            
            if (!result.isSuccess()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "errors", result.getErrors(),
                    "suggestions", result.getSuggestions()
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "imported", result.getImported(),
                "skipped", result.getSkipped(),
                "message", "Successfully imported " + result.getImported() + " test cases"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Import failed: " + e.getMessage()));
        }
    }
    
    /**
     * Get all test cases
     * 
     * GET /api/testcases
     */
    @GetMapping
    public ResponseEntity<?> getAllTestCases(
            @RequestParam(required = false) String organization,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String priority) {
        
        try {
            List<TestCase> testCases = testCaseService.getAllTestCases();
            
            return ResponseEntity.ok(Map.of(
                "testCases", testCases,
                "total", testCases.size()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get test cases: " + e.getMessage()));
        }
    }
    
    /**
     * Get single test case by ID
     * 
     * GET /api/testcases/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getTestCase(@PathVariable String id) {
        try {
            TestCase testCase = testCaseService.getTestCaseById(id);
            
            if (testCase == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(testCase);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get test case: " + e.getMessage()));
        }
    }
    
    /**
     * Get coverage statistics
     * 
     * GET /api/testcases/stats/coverage
     */
    @GetMapping("/stats/coverage")
    public ResponseEntity<?> getCoverageStats() {
        try {
            TestCaseService.CoverageStats stats = testCaseService.getCoverageStats();
            
            return ResponseEntity.ok(Map.of(
                "total", stats.getTotalTestCases(),
                "automated", stats.getAutomatedTestCases(),
                "manual", stats.getManualTestCases(),
                "coveragePercentage", stats.getCoveragePercentage()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get coverage stats: " + e.getMessage()));
        }
    }
    
    /**
     * Get untested test cases (gaps)
     * 
     * GET /api/testcases/gaps
     */
    @GetMapping("/gaps")
    public ResponseEntity<?> getUntestedCases() {
        try {
            List<TestCase> untested = testCaseService.getUntestedCases();
            
            return ResponseEntity.ok(Map.of(
                "untestedCases", untested,
                "count", untested.size()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get untested cases: " + e.getMessage()));
        }
    }
    
    /**
     * Delete test case
     * 
     * DELETE /api/testcases/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTestCase(@PathVariable String id) {
        try {
            boolean deleted = testCaseService.deleteTestCase(id);
            
            if (!deleted) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Test case deleted"));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to delete test case: " + e.getMessage()));
        }
    }
    
    /**
     * Helper: Check if file is Excel
     */
    private boolean isExcelFile(String filename) {
        if (filename == null) {
            return false;
        }
        String lower = filename.toLowerCase();
        return lower.endsWith(".xlsx") || lower.endsWith(".xls");
    }
}

