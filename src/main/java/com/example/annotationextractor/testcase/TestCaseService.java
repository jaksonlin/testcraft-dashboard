package com.example.annotationextractor.testcase;

import com.example.annotationextractor.casemodel.TestMethodInfo;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for test case management and import operations.
 * Business logic layer for test case features.
 */
@Service
public class TestCaseService {
    
    private final TestCaseRepository testCaseRepository;
    private final ExcelParserService excelParserService;
    
    public TestCaseService(TestCaseRepository testCaseRepository) {
        this.testCaseRepository = testCaseRepository;
        this.excelParserService = new ExcelParserService();
    }
    
    /**
     * Preview Excel file before import
     */
    public ExcelParserService.ExcelPreview previewExcel(InputStream inputStream, String filename) throws Exception {
        return excelParserService.previewExcel(inputStream, filename);
    }
    
    /**
     * Validate column mappings
     */
    public ExcelParserService.ValidationResult validateMappings(Map<String, String> mappings, List<String> columns) {
        return excelParserService.validateMappings(mappings, columns);
    }
    
    /**
     * Import test cases from Excel
     */
    public ImportResult importTestCases(
            InputStream inputStream,
            String filename,
            Map<String, String> columnMappings,
            int dataStartRow,
            boolean replaceExisting,
            String createdBy,
            String organization) throws Exception {
        
        // Read upload into memory once so we can create fresh streams for each pass
        byte[] bytes;
        if (inputStream instanceof ByteArrayInputStream) {
            // If already a ByteArrayInputStream, read remaining bytes then reconstruct
            bytes = inputStream.readAllBytes();
        } else {
            bytes = inputStream.readAllBytes();
        }

        // Validate mappings first using a fresh stream
        List<String> columns = excelParserService.previewExcel(new ByteArrayInputStream(bytes), filename).getColumns();
        ExcelParserService.ValidationResult validation = validateMappings(columnMappings, columns);
        
        if (!validation.isValid()) {
            return new ImportResult(
                false,
                0,
                0,
                validation.getMissingRequiredFields(),
                validation.getSuggestions()
            );
        }
        
        // Parse Excel with mappings with another fresh stream and collect row errors
        ExcelParserService.ParseResult parseResult = excelParserService.parseWithMappingsDetailed(
            new ByteArrayInputStream(bytes),
            filename,
            columnMappings,
            dataStartRow
        );
        List<TestCase> testCases = parseResult.getValidTestCases();
        
        // Filter valid test cases
        List<TestCase> validTestCases = testCases.stream()
            .filter(TestCase::isValid)
            .collect(Collectors.toList());
        
        // Set metadata
        for (TestCase testCase : validTestCases) {
            testCase.setCreatedBy(createdBy);
            testCase.setOrganization(organization);
        }
        
        // Import to database
        int imported = 0;
        int skipped = testCases.size() - validTestCases.size();
        
        try {
            if (!parseResult.getRowErrors().isEmpty()) {
                // If any row errors exist, fail the import and report them
                return new ImportResult(false, 0, testCases.size(), parseResult.getRowErrors(), List.of());
            }
            imported = testCaseRepository.saveAll(validTestCases);
        } catch (SQLException e) {
            return new ImportResult(
                false,
                0,
                testCases.size(),
                List.of("Database error: " + e.getMessage()),
                List.of()
            );
        }
        
        return new ImportResult(true, imported, skipped, List.of(), List.of());
    }
    
    /**
     * Link test methods to test cases based on test case IDs
     */
    public int linkTestMethodsToCases(List<TestMethodInfo> testMethods, String repositoryName) throws SQLException {
        int linked = 0;
        
        for (TestMethodInfo method : testMethods) {
            String[] testCaseIds = method.getTestCaseIds();
            
            if (testCaseIds != null && testCaseIds.length > 0) {
                for (String testCaseId : testCaseIds) {
                    // Check if test case exists
                    TestCase testCase = testCaseRepository.findById(testCaseId);
                    
                    if (testCase != null) {
                        testCaseRepository.linkTestCaseToMethod(
                            testCaseId,
                            repositoryName,
                            method.getPackageName(),
                            method.getClassName(),
                            method.getMethodName(),
                            method.getFilePath(),
                            method.getLineNumber()
                        );
                        linked++;
                    }
                }
            }
        }
        
        return linked;
    }
    
    /**
     * Get all test cases
     */
    public List<TestCase> getAllTestCases() throws SQLException {
        return testCaseRepository.findAll();
    }

    public List<TestCase> getAllTestCasesPaged(Integer page, Integer size, String organization, String type, String priority) throws SQLException {
        int pageNum = page != null && page >= 0 ? page : 0;
        int pageSize = size != null && size > 0 ? size : 20;
        int offset = pageNum * pageSize;
        return testCaseRepository.findAllPaged(organization, type, priority, offset, pageSize);
    }
    
    /**
     * Get test case by ID
     */
    public TestCase getTestCaseById(String id) throws SQLException {
        return testCaseRepository.findById(id);
    }
    
    /**
     * Get coverage statistics
     */
    public CoverageStats getCoverageStats() throws SQLException {
        int total = testCaseRepository.countAll();
        int automated = testCaseRepository.countWithCoverage();
        int manual = total - automated;
        double percentage = total > 0 ? (double) automated / total * 100 : 0.0;
        
        return new CoverageStats(total, automated, manual, percentage);
    }

    public int countUntestedCases() throws SQLException {
        return testCaseRepository.countWithoutCoverage();
    }
    
    /**
     * Get test cases without coverage (gaps)
     */
    public List<TestCase> getUntestedCases() throws SQLException {
        return testCaseRepository.findWithoutCoverage();
    }

    public List<TestCase> getUntestedCasesPaged(Integer page, Integer size) throws SQLException {
        int pageNum = page != null && page >= 0 ? page : 0;
        int pageSize = size != null && size > 0 ? size : 20;
        int offset = pageNum * pageSize;
        return testCaseRepository.findWithoutCoveragePaged(offset, pageSize);
    }
    
    /**
     * Delete test case by ID
     */
    public boolean deleteTestCase(String id) throws SQLException {
        return testCaseRepository.deleteById(id);
    }
    
    /**
     * Import result model
     */
    public static class ImportResult {
        private final boolean success;
        private final int imported;
        private final int skipped;
        private final List<String> errors;
        private final List<String> suggestions;
        
        public ImportResult(boolean success, int imported, int skipped, List<String> errors, List<String> suggestions) {
            this.success = success;
            this.imported = imported;
            this.skipped = skipped;
            this.errors = errors;
            this.suggestions = suggestions;
        }
        
        public boolean isSuccess() { return success; }
        public int getImported() { return imported; }
        public int getSkipped() { return skipped; }
        public List<String> getErrors() { return errors; }
        public List<String> getSuggestions() { return suggestions; }
    }
    
    /**
     * Coverage statistics model
     */
    public static class CoverageStats {
        private final int totalTestCases;
        private final int automatedTestCases;
        private final int manualTestCases;
        private final double coveragePercentage;
        
        public CoverageStats(int total, int automated, int manual, double percentage) {
            this.totalTestCases = total;
            this.automatedTestCases = automated;
            this.manualTestCases = manual;
            this.coveragePercentage = percentage;
        }
        
        public int getTotalTestCases() { return totalTestCases; }
        public int getAutomatedTestCases() { return automatedTestCases; }
        public int getManualTestCases() { return manualTestCases; }
        public double getCoveragePercentage() { return coveragePercentage; }
    }
}

