package com.example.annotationextractor.testcase;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.InputStream;
import java.util.*;

/**
 * Service for parsing Excel files in a format-agnostic way.
 * 
 * Features:
 * - Auto-detects column headers
 * - Suggests mappings based on common patterns
 * - Handles any Excel structure through user-defined mappings
 * - Supports both .xls and .xlsx formats
 */
public class ExcelParserService {
    
    // Common column name patterns for auto-detection
    private static final Map<String, List<String>> COLUMN_PATTERNS = Map.ofEntries(
        Map.entry("id", List.of("id", "test_id", "testid", "case_id", "caseid", "tc_id", "number", "#", "key")),
        Map.entry("title", List.of("title", "name", "test_name", "testname", "case_name", "summary", "description", "scenario")),
        Map.entry("steps", List.of("steps", "test_steps", "teststeps", "procedure", "actions", "how_to_test", "when", "execution")),
        Map.entry("setup", List.of("setup", "precondition", "pre-condition", "prerequisite", "prerequisites", "given", "before")),
        Map.entry("teardown", List.of("teardown", "postcondition", "post-condition", "cleanup", "after")),
        Map.entry("expected_result", List.of("expected", "expected_result", "expectedresult", "result", "verification", "then", "should")),
        Map.entry("priority", List.of("priority", "importance", "severity", "criticality")),
        Map.entry("type", List.of("type", "category", "test_type", "testtype", "kind")),
        Map.entry("status", List.of("status", "state", "condition"))
    );
    
    /**
     * Parse Excel file and extract column headers
     */
    public ExcelPreview previewExcel(InputStream inputStream, String filename) throws Exception {
        try (Workbook workbook = createWorkbook(inputStream, filename)) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // Find header row (usually first non-empty row)
            int headerRow = findHeaderRow(sheet);
            
            // Extract column names
            List<String> columns = extractColumns(sheet, headerRow);
            
            // Get preview data (first 10 rows)
            List<Map<String, String>> previewData = extractPreviewData(sheet, headerRow, columns, 10);
            
            // Auto-detect column mappings
            Map<String, String> suggestedMappings = autoDetectMappings(columns);
            Map<String, Integer> confidence = calculateConfidence(suggestedMappings, columns);
            
            return new ExcelPreview(columns, previewData, suggestedMappings, confidence, headerRow + 1);
        }
    }
    
    /**
     * Parse Excel with user-defined column mappings
     */
    public List<TestCase> parseWithMappings(
            InputStream inputStream,
            String filename,
            Map<String, String> columnMappings,
            int dataStartRow) throws Exception {
        
        List<TestCase> testCases = new ArrayList<>();
        
        try (Workbook workbook = createWorkbook(inputStream, filename)) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // Get column indexes
            Map<String, Integer> columnIndexes = buildColumnIndexes(sheet, dataStartRow - 1, columnMappings);
            
            // Parse data rows
            for (int i = dataStartRow; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isEmptyRow(row)) {
                    continue;
                }
                
                TestCase testCase = parseRow(row, columnIndexes, columnMappings);
                if (testCase != null && testCase.isValid()) {
                    testCases.add(testCase);
                }
            }
        }
        
        return testCases;
    }
    
    /**
     * Find header row (first non-empty row with multiple columns)
     */
    private int findHeaderRow(Sheet sheet) {
        for (int i = 0; i <= Math.min(10, sheet.getLastRowNum()); i++) {
            Row row = sheet.getRow(i);
            if (row != null && getFilledCellCount(row) >= 3) {
                return i;
            }
        }
        return 0; // Default to first row
    }
    
    /**
     * Extract column names from header row
     */
    private List<String> extractColumns(Sheet sheet, int headerRow) {
        List<String> columns = new ArrayList<>();
        Row row = sheet.getRow(headerRow);
        
        if (row != null) {
            for (Cell cell : row) {
                String value = getCellValueAsString(cell);
                columns.add(value != null ? value.trim() : "Column " + (cell.getColumnIndex() + 1));
            }
        }
        
        return columns;
    }
    
    /**
     * Extract preview data rows
     */
    private List<Map<String, String>> extractPreviewData(Sheet sheet, int headerRow, List<String> columns, int maxRows) {
        List<Map<String, String>> preview = new ArrayList<>();
        
        int count = 0;
        for (int i = headerRow + 1; i <= sheet.getLastRowNum() && count < maxRows; i++) {
            Row row = sheet.getRow(i);
            if (row == null || isEmptyRow(row)) {
                continue;
            }
            
            Map<String, String> rowData = new LinkedHashMap<>();
            for (int j = 0; j < columns.size(); j++) {
                Cell cell = row.getCell(j);
                rowData.put(columns.get(j), getCellValueAsString(cell));
            }
            
            preview.add(rowData);
            count++;
        }
        
        return preview;
    }
    
    /**
     * Auto-detect column mappings based on common patterns
     * Uses best-match algorithm to handle ambiguous column names
     */
    public Map<String, String> autoDetectMappings(List<String> excelColumns) {
        Map<String, String> mappings = new HashMap<>();
        
        for (String excelCol : excelColumns) {
            String normalized = normalizeColumnName(excelCol);
            
            // Find best match with highest score
            String bestMatch = null;
            int bestScore = 0;
            
            for (Map.Entry<String, List<String>> entry : COLUMN_PATTERNS.entrySet()) {
                String systemField = entry.getKey();
                List<String> patterns = entry.getValue();
                
                for (String pattern : patterns) {
                    String normalizedPattern = normalizeColumnName(pattern);
                    int score = calculateMatchScore(normalized, normalizedPattern);
                    
                    if (score > bestScore) {
                        bestScore = score;
                        bestMatch = systemField;
                    }
                }
            }
            
            // Only map if we have a good match (score >= 50)
            if (bestMatch != null && bestScore >= 50) {
                mappings.put(excelCol, bestMatch);
            }
        }
        
        return mappings;
    }
    
    /**
     * Calculate match score between normalized column name and pattern
     * Higher score = better match
     */
    private int calculateMatchScore(String normalized, String pattern) {
        if (normalized.equals(pattern)) {
            return 100; // Perfect match
        }
        
        if (normalized.contains(pattern) && pattern.length() >= 3) {
            // Column contains pattern - score based on how much of the column it covers
            int coverage = (pattern.length() * 100) / normalized.length();
            return Math.min(95, 50 + coverage / 2);
        }
        
        if (pattern.contains(normalized) && normalized.length() >= 2) {
            // Pattern contains column - slightly lower score
            return 70;
        }
        
        return 0; // No match
    }
    
    /**
     * Calculate confidence scores for mappings
     */
    private Map<String, Integer> calculateConfidence(Map<String, String> mappings, List<String> columns) {
        Map<String, Integer> confidence = new HashMap<>();
        
        for (Map.Entry<String, String> entry : mappings.entrySet()) {
            String excelCol = entry.getKey();
            String systemField = entry.getValue();
            
            String normalized = normalizeColumnName(excelCol);
            List<String> patterns = COLUMN_PATTERNS.get(systemField);
            
            int score = 50; // Default
            
            if (patterns != null) {
                for (String pattern : patterns) {
                    String normalizedPattern = normalizeColumnName(pattern);
                    
                    if (normalized.equals(normalizedPattern)) {
                        score = 100; // Exact match
                        break;
                    } else if (normalized.contains(normalizedPattern)) {
                        score = 90; // Contains pattern
                    } else if (normalizedPattern.contains(normalized)) {
                        score = 80; // Pattern contains column
                    }
                }
            }
            
            confidence.put(excelCol, score);
        }
        
        return confidence;
    }
    
    /**
     * Build column index map
     */
    private Map<String, Integer> buildColumnIndexes(Sheet sheet, int headerRow, Map<String, String> columnMappings) {
        Map<String, Integer> indexes = new HashMap<>();
        Row row = sheet.getRow(headerRow);
        
        if (row != null) {
            for (Cell cell : row) {
                String columnName = getCellValueAsString(cell);
                if (columnName != null && columnMappings.containsKey(columnName)) {
                    String systemField = columnMappings.get(columnName);
                    indexes.put(systemField, cell.getColumnIndex());
                }
            }
        }
        
        return indexes;
    }
    
    /**
     * Parse a single row into a TestCase
     */
    private TestCase parseRow(Row row, Map<String, Integer> columnIndexes, Map<String, String> columnMappings) {
        TestCase testCase = new TestCase();
        
        // Extract mapped fields
        testCase.setId(getCellValue(row, columnIndexes.get("id")));
        testCase.setTitle(getCellValue(row, columnIndexes.get("title")));
        testCase.setSteps(getCellValue(row, columnIndexes.get("steps")));
        testCase.setSetup(getCellValue(row, columnIndexes.get("setup")));
        testCase.setTeardown(getCellValue(row, columnIndexes.get("teardown")));
        testCase.setExpectedResult(getCellValue(row, columnIndexes.get("expected_result")));
        testCase.setPriority(getCellValue(row, columnIndexes.get("priority")));
        testCase.setType(getCellValue(row, columnIndexes.get("type")));
        testCase.setStatus(getCellValue(row, columnIndexes.get("status")));
        
        // Extract custom fields (unmapped columns)
        Map<String, Object> customFields = new HashMap<>();
        Set<String> mappedColumns = new HashSet<>(columnMappings.keySet());
        
        for (Cell cell : row) {
            String columnName = getColumnName(row.getSheet(), cell.getColumnIndex());
            if (columnName != null && !mappedColumns.contains(columnName)) {
                String value = getCellValueAsString(cell);
                if (value != null && !value.trim().isEmpty()) {
                    customFields.put(columnName, value);
                }
            }
        }
        
        testCase.setCustomFields(customFields);
        
        return testCase;
    }
    
    /**
     * Helper: Create workbook from input stream
     */
    private Workbook createWorkbook(InputStream inputStream, String filename) throws Exception {
        if (filename.toLowerCase().endsWith(".xlsx")) {
            return new XSSFWorkbook(inputStream);
        } else {
            return new HSSFWorkbook(inputStream);
        }
    }
    
    /**
     * Helper: Get cell value as string
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }
    
    /**
     * Helper: Get cell value with null check
     */
    private String getCellValue(Row row, Integer columnIndex) {
        if (columnIndex == null) {
            return null;
        }
        Cell cell = row.getCell(columnIndex);
        return getCellValueAsString(cell);
    }
    
    /**
     * Helper: Get column name from sheet
     */
    private String getColumnName(Sheet sheet, int columnIndex) {
        Row headerRow = sheet.getRow(0);
        if (headerRow != null) {
            Cell cell = headerRow.getCell(columnIndex);
            return getCellValueAsString(cell);
        }
        return null;
    }
    
    /**
     * Helper: Normalize column name for comparison
     */
    private String normalizeColumnName(String name) {
        if (name == null) {
            return "";
        }
        return name.toLowerCase().replaceAll("[\\s_-]", "");
    }
    
    /**
     * Helper: Check if row is empty
     */
    private boolean isEmptyRow(Row row) {
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellValueAsString(cell);
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Helper: Count filled cells in row
     */
    private int getFilledCellCount(Row row) {
        int count = 0;
        for (Cell cell : row) {
            String value = getCellValueAsString(cell);
            if (value != null && !value.trim().isEmpty()) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Validate that all required fields are mapped
     * 
     * @param columnMappings User's column mappings (excelColumn -> systemField)
     * @param excelColumns All available Excel columns
     * @return Validation result with missing fields and suggestions
     */
    public ValidationResult validateMappings(Map<String, String> columnMappings, List<String> excelColumns) {
        List<String> missingRequired = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        
        // Check required fields: id, title, steps
        boolean hasId = columnMappings.containsValue("id");
        boolean hasTitle = columnMappings.containsValue("title");
        boolean hasSteps = columnMappings.containsValue("steps");
        
        if (!hasId) {
            missingRequired.add("ID");
        }
        if (!hasTitle) {
            missingRequired.add("Title");
        }
        if (!hasSteps) {
            missingRequired.add("Steps");
        }
        
        // If required fields missing, try to suggest from unmapped columns
        if (!missingRequired.isEmpty()) {
            Set<String> mappedColumns = columnMappings.keySet();
            List<String> unmappedColumns = excelColumns.stream()
                .filter(col -> !mappedColumns.contains(col))
                .collect(java.util.stream.Collectors.toList());
            
            // Try to suggest from unmapped columns
            for (String unmapped : unmappedColumns) {
                String normalized = normalizeColumnName(unmapped);
                
                if (!hasId && couldBeId(normalized)) {
                    suggestions.add("Column '" + unmapped + "' might be ID");
                }
                if (!hasTitle && couldBeTitle(normalized)) {
                    suggestions.add("Column '" + unmapped + "' might be Title");
                }
                if (!hasSteps && couldBeSteps(normalized)) {
                    suggestions.add("Column '" + unmapped + "' might be Steps");
                }
            }
        }
        
        return new ValidationResult(
            missingRequired.isEmpty(),
            missingRequired,
            suggestions
        );
    }
    
    /**
     * Check if a normalized column name could be an ID field
     */
    private boolean couldBeId(String normalized) {
        return normalized.contains("id") || 
               normalized.contains("number") || 
               normalized.contains("key") ||
               normalized.equals("#") ||
               normalized.contains("case") ||
               normalized.contains("test");
    }
    
    /**
     * Check if a normalized column name could be a Title field
     */
    private boolean couldBeTitle(String normalized) {
        return normalized.contains("title") || 
               normalized.contains("name") || 
               normalized.contains("summary") ||
               normalized.contains("description") ||
               normalized.contains("scenario");
    }
    
    /**
     * Check if a normalized column name could be a Steps field
     */
    private boolean couldBeSteps(String normalized) {
        return normalized.contains("step") || 
               normalized.contains("procedure") || 
               normalized.contains("action") ||
               normalized.contains("execution") ||
               normalized.contains("when") ||
               normalized.contains("how");
    }
    
    /**
     * Preview response model
     */
    public static class ExcelPreview {
        private final List<String> columns;
        private final List<Map<String, String>> previewData;
        private final Map<String, String> suggestedMappings;
        private final Map<String, Integer> confidence;
        private final int suggestedDataStartRow;
        
        public ExcelPreview(List<String> columns, List<Map<String, String>> previewData,
                          Map<String, String> suggestedMappings, Map<String, Integer> confidence,
                          int suggestedDataStartRow) {
            this.columns = columns;
            this.previewData = previewData;
            this.suggestedMappings = suggestedMappings;
            this.confidence = confidence;
            this.suggestedDataStartRow = suggestedDataStartRow;
        }
        
        public List<String> getColumns() { return columns; }
        public List<Map<String, String>> getPreviewData() { return previewData; }
        public Map<String, String> getSuggestedMappings() { return suggestedMappings; }
        public Map<String, Integer> getConfidence() { return confidence; }
        public int getSuggestedDataStartRow() { return suggestedDataStartRow; }
    }
    
    /**
     * Validation result model
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> missingRequiredFields;
        private final List<String> suggestions;
        
        public ValidationResult(boolean valid, List<String> missingRequiredFields, List<String> suggestions) {
            this.valid = valid;
            this.missingRequiredFields = missingRequiredFields;
            this.suggestions = suggestions;
        }
        
        public boolean isValid() { return valid; }
        public List<String> getMissingRequiredFields() { return missingRequiredFields; }
        public List<String> getSuggestions() { return suggestions; }
        
        @Override
        public String toString() {
            if (valid) {
                return "ValidationResult{valid=true}";
            } else {
                return "ValidationResult{valid=false, missing=" + missingRequiredFields + 
                       ", suggestions=" + suggestions + "}";
            }
        }
    }
}

