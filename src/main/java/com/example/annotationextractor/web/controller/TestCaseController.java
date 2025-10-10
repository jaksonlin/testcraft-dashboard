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
@RequestMapping("/testcases")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
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
            if (file.isEmpty() || file.getSize() == 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
            }
            
            String filename = file.getOriginalFilename();
            if (!isExcelFile(filename)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Only Excel files (.xls, .xlsx) are supported"));
            }
            
            // Debug diagnostics
            try {
                String contentType = String.valueOf(file.getContentType());
                long size = file.getSize();
                System.out.println("[TestCases] Upload preview: name=" + filename + ", size=" + size + ", contentType=" + contentType);
            } catch (Exception ignore) { }

            // Read fully to avoid stream issues with some servlet containers
            java.io.ByteArrayInputStream bin = new java.io.ByteArrayInputStream(file.getBytes());
            ExcelParserService.ExcelPreview preview = testCaseService.previewExcel(
                bin,
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
                "suggestedHeaderRow", preview.getSuggestedHeaderRow(),
                "suggestedDataStartRow", preview.getSuggestedDataStartRow(),
                "totalRows", preview.getTotalRows(),
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

    @PostMapping("/upload/preview-with-rows")
    public ResponseEntity<?> previewExcelWithRows(
            @RequestParam("file") MultipartFile file,
            @RequestParam("headerRow") int headerRow,
            @RequestParam("dataStartRow") int dataStartRow) {
        try {
            if (file.isEmpty() || file.getSize() == 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
            }
            
            String filename = file.getOriginalFilename();
            if (!isExcelFile(filename)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Only Excel files (.xls, .xlsx) are supported"));
            }
            
            // Read fully to avoid stream issues with some servlet containers
            java.io.ByteArrayInputStream bin = new java.io.ByteArrayInputStream(file.getBytes());
            ExcelParserService.ExcelPreview preview = testCaseService.previewExcelWithRows(
                bin,
                filename,
                headerRow,
                dataStartRow
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
                "suggestedHeaderRow", preview.getSuggestedHeaderRow(),
                "suggestedDataStartRow", preview.getSuggestedDataStartRow(),
                "totalRows", preview.getTotalRows(),
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
            @RequestParam(value = "headerRow", defaultValue = "0") int headerRow,
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
            java.io.ByteArrayInputStream importIn = new java.io.ByteArrayInputStream(file.getBytes());
            TestCaseService.ImportResult result = testCaseService.importTestCases(
                importIn,
                file.getOriginalFilename(),
                columnMappings,
                headerRow,
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
                "created", result.getCreated(),
                "updated", result.getUpdated(),
                "skipped", result.getSkipped(),
                "message", "Successfully imported " + result.getImported() + " test cases (" + 
                          result.getCreated() + " created, " + result.getUpdated() + " updated)"
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
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        
        try {
            int pageNum = page != null && page >= 0 ? page : 0;
            int pageSize = size != null && size > 0 ? size : 20;

            List<TestCase> testCases = testCaseService.getAllTestCasesPaged(pageNum, pageSize, organization, type, priority, teamId);
            int total = testCaseService.countTestCases(organization, type, priority, teamId); // Count with same filters

            return ResponseEntity.ok(Map.of(
                "content", testCases,
                "page", pageNum,
                "size", pageSize,
                "total", total
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get test cases: " + e.getMessage()));
        }
    }
    
    /**
     * Get single test case by internal ID
     * 
     * GET /api/testcases/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getTestCase(@PathVariable Long id) {
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
    public ResponseEntity<?> getUntestedCases(@RequestParam(required = false) Integer page,
                                              @RequestParam(required = false) Integer size) {
        try {
            int pageNum = page != null && page >= 0 ? page : 0;
            int pageSize = size != null && size > 0 ? size : 20;

            List<TestCase> untested = testCaseService.getUntestedCasesPaged(pageNum, pageSize);
            int total = testCaseService.countUntestedCases();

            return ResponseEntity.ok(Map.of(
                "content", untested,
                "page", pageNum,
                "size", pageSize,
                "total", total
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get untested cases: " + e.getMessage()));
        }
    }
    
    /**
     * Get distinct organizations for filter dropdown
     * 
     * GET /api/testcases/organizations
     */
    @GetMapping("/organizations")
    public ResponseEntity<?> getOrganizations() {
        try {
            List<String> organizations = testCaseService.getDistinctOrganizations();
            return ResponseEntity.ok(organizations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get organizations: " + e.getMessage()));
        }
    }
    
    /**
     * Delete test case by internal ID
     * 
     * DELETE /api/testcases/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTestCase(@PathVariable Long id) {
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

