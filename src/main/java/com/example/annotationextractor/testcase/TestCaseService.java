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
    
    public ExcelParserService.ExcelPreview previewExcelWithRows(InputStream inputStream, String filename, int headerRow, int dataStartRow) throws Exception {
        return excelParserService.previewExcelWithRows(inputStream, filename, headerRow, dataStartRow);
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
            int headerRow,
            int dataStartRow,
            boolean replaceExisting,
            String createdBy,
            String organization,
            Long teamId) throws Exception {
        
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
            headerRow,
            dataStartRow
        );
        List<TestCase> testCases = parseResult.getValidTestCases();
        
        // Filter valid test cases
        List<TestCase> validTestCases = testCases.stream()
            .filter(TestCase::isValid)
            .collect(Collectors.toList());
        
        // Set metadata and resolve team names to team IDs
        for (TestCase testCase : validTestCases) {
            testCase.setCreatedBy(createdBy);
            
            // Organization: UI selection always overrides Excel (if provided)
            if (organization != null && !organization.trim().isEmpty()) {
                testCase.setOrganization(organization);
            }
            // Fallback to Excel organization if UI didn't specify
            else if (testCase.getOrganization() == null || testCase.getOrganization().trim().isEmpty()) {
                testCase.setOrganization("default");
            }
            
            // Team: UI selection always overrides Excel (if provided)
            if (teamId != null) {
                testCase.setTeamId(teamId);
            }
            // Fallback to Excel team if UI didn't specify
            else if (testCase.getTeamName() != null && !testCase.getTeamName().trim().isEmpty()) {
                try {
                    Long lookupTeamId = testCaseRepository.findTeamIdByName(testCase.getTeamName());
                    if (lookupTeamId != null) {
                        testCase.setTeamId(lookupTeamId);
                    }
                    // Note: teamName is kept for display purposes even if lookup fails
                } catch (SQLException e) {
                    // Log warning but don't fail import - team assignment can be done later
                    System.err.println("Warning: Could not lookup team '" + testCase.getTeamName() + "': " + e.getMessage());
                }
            }
        }
        
        // Import to database
        int skipped = testCases.size() - validTestCases.size();
        TestCaseRepository.SaveResult saveResult;
        
        try {
            if (!parseResult.getRowErrors().isEmpty()) {
                // If any row errors exist, fail the import and report them
                return new ImportResult(false, 0, 0, testCases.size(), parseResult.getRowErrors(), List.of());
            }
            saveResult = testCaseRepository.saveAll(validTestCases);
        } catch (SQLException e) {
            return new ImportResult(
                false,
                0,
                0,
                testCases.size(),
                List.of("Database error: " + e.getMessage()),
                List.of()
            );
        }
        
        return new ImportResult(
            true, 
            saveResult.getCreated(), 
            saveResult.getUpdated(), 
            skipped, 
            List.of(), 
            List.of()
        );
    }
    
    /**
     * Link test methods to test cases based on test case IDs (external IDs from annotations)
     * Note: TestMethodInfo contains external test case IDs, we need to look up internal IDs for linkage
     */
    public int linkTestMethodsToCases(List<TestMethodInfo> testMethods, String repositoryName) throws SQLException {
        int linked = 0;
        
        for (TestMethodInfo method : testMethods) {
            String[] testCaseIds = method.getTestCaseIds();
            
            if (testCaseIds != null && testCaseIds.length > 0) {
                for (String externalTestCaseId : testCaseIds) {
                    // Look up test case by external ID (first try without organization filter for simplicity)
                    TestCase testCase = testCaseRepository.findByIdLegacy(externalTestCaseId);
                    
                    if (testCase != null && testCase.getInternalId() != null) {
                        // Link using internal ID
                        testCaseRepository.linkTestCaseToMethod(
                            testCase.getInternalId(),
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

    public List<TestCase> getAllTestCasesPaged(Integer page, Integer size, String organization, String type, String priority, Long teamId, String status, String search) throws SQLException {
        int pageNum = page != null && page >= 0 ? page : 0;
        int pageSize = size != null && size > 0 ? size : 20;
        int offset = pageNum * pageSize;
        return testCaseRepository.findAllPaged(organization, type, priority, teamId, status, search, offset, pageSize);
    }
    
    /**
     * Count test cases with filters (for pagination)
     */
    public int countTestCases(String organization, String type, String priority, Long teamId, String status, String search) throws SQLException {
        return testCaseRepository.countAll(organization, type, priority, teamId, status, search);
    }
    
    /**
     * Get test case by internal ID (primary key)
     */
    public TestCase getTestCaseById(Long internalId) throws SQLException {
        return testCaseRepository.findById(internalId);
    }
    
    /**
     * Get test case by external ID (from test management system)
     * @deprecated Use getTestCaseById(Long) with internal ID instead
     */
    @Deprecated
    public TestCase getTestCaseByExternalId(String externalId) throws SQLException {
        return testCaseRepository.findByIdLegacy(externalId);
    }
    
    /**
     * Get test case by external ID with organization
     */
    public TestCase getTestCaseByExternalId(String externalId, String organization) throws SQLException {
        return testCaseRepository.findByExternalId(externalId, organization);
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
     * Delete test case by internal ID
     */
    public boolean deleteTestCase(Long internalId) throws SQLException {
        return testCaseRepository.deleteById(internalId);
    }
    
    /**
     * Delete test case by external ID and organization
     */
    public boolean deleteTestCaseByExternalId(String externalId, String organization) throws SQLException {
        return testCaseRepository.deleteByExternalId(externalId, organization);
    }
    
    /**
     * Get distinct organizations for filter dropdown
     */
    public List<String> getDistinctOrganizations() throws SQLException {
        return testCaseRepository.findDistinctOrganizations();
    }
    
    /**
     * Get all teams for filter dropdown
     */
    public List<Map<String, Object>> getAllTeams() throws SQLException {
        return testCaseRepository.findAllTeams();
    }
    
    /**
     * Import result model
     */
    public static class ImportResult {
        private final boolean success;
        private final int created;
        private final int updated;
        private final int skipped;
        private final List<String> errors;
        private final List<String> suggestions;
        
        public ImportResult(boolean success, int created, int updated, int skipped, List<String> errors, List<String> suggestions) {
            this.success = success;
            this.created = created;
            this.updated = updated;
            this.skipped = skipped;
            this.errors = errors;
            this.suggestions = suggestions;
        }
        
        public boolean isSuccess() { return success; }
        public int getCreated() { return created; }
        public int getUpdated() { return updated; }
        public int getImported() { return created + updated; } // Total for backward compatibility
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

