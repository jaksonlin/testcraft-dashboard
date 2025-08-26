package com.example.annotationextractor.reporting;

import com.example.annotationextractor.database.DatabaseConfig;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Generates Excel reports for test analytics data
 */
public class ExcelReportGenerator {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
        /**
     * Generate a comprehensive weekly report
     */
    public static void generateWeeklyReport(String outputPath) throws IOException, SQLException {
        try (Workbook workbook = new XSSFWorkbook()) {
            
            // Create summary sheet
            Sheet summarySheet = workbook.createSheet("Weekly Summary");
            createSummarySheet(workbook, summarySheet);
            
            // Create repository details sheet
            Sheet repoSheet = workbook.createSheet("Repository Details");
            createRepositoryDetailsSheet(workbook, repoSheet);
            
            // Create trends sheet
            Sheet trendsSheet = workbook.createSheet("Trends & Analysis");
            createTrendsSheet(workbook, trendsSheet);
            
            // Create annotation coverage sheet
            Sheet coverageSheet = workbook.createSheet("Annotation Coverage");
            createCoverageSheet(workbook, coverageSheet);
            
            // Create test method details sheet
            Sheet testMethodSheet = workbook.createSheet("Test Method Details");
            createTestMethodDetailsSheet(workbook, testMethodSheet);
            
            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream(outputPath)) {
                workbook.write(fileOut);
            }
            
            System.out.println("Weekly report generated successfully: " + outputPath);
        }
    }
    
    /**
     * Create the summary sheet
     */
    private static void createSummarySheet(Workbook workbook, Sheet sheet) throws SQLException {
        // Set column widths
        sheet.setColumnWidth(0, 3000);
        sheet.setColumnWidth(1, 4000);
        sheet.setColumnWidth(2, 3000);
        
        // Create title
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Test Analytics Weekly Report");
        
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleCell.setCellStyle(titleStyle);
        
        // Merge title cells
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));
        
        // Create report info
        int rowNum = 2;
        createInfoRow(sheet, rowNum++, "Report Generated", TIMESTAMP_FORMAT.format(new Date()));
        createInfoRow(sheet, rowNum++, "Report Period", "Weekly");
        
        // Get current metrics
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM daily_metrics WHERE metric_date = CURRENT_DATE")) {
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    rowNum++;
                    createInfoRow(sheet, rowNum++, "Total Repositories", rs.getInt("total_repositories"));
                    createInfoRow(sheet, rowNum++, "Total Test Classes", rs.getInt("total_test_classes"));
                    createInfoRow(sheet, rowNum++, "Total Test Methods", rs.getInt("total_test_methods"));
                    createInfoRow(sheet, rowNum++, "Total Annotated Methods", rs.getInt("total_annotated_methods"));
                    createInfoRow(sheet, rowNum++, "Overall Coverage Rate", 
                                String.format("%.2f%%", rs.getDouble("overall_coverage_rate")));
                }
            }
        }
        
        // Create summary chart
        createSummaryChart(workbook, sheet, rowNum + 2);
    }
    
    /**
     * Create repository details sheet
     */
    private static void createRepositoryDetailsSheet(Workbook workbook, Sheet sheet) throws SQLException {
        // Set column widths
        sheet.setColumnWidth(0, 3000);
        sheet.setColumnWidth(1, 4000);
        sheet.setColumnWidth(2, 5000);
        sheet.setColumnWidth(3, 3000);
        sheet.setColumnWidth(4, 3000);
        sheet.setColumnWidth(5, 3000);
        sheet.setColumnWidth(6, 3000);
        
        // Create headers
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Repository", "Path", "Test Classes", "Test Methods", "Annotated", "Coverage %", "Last Scan"};
        
        CellStyle headerStyle = createHeaderStyle(workbook);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Get repository data
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM repositories ORDER BY annotation_coverage_rate DESC")) {
            
            try (ResultSet rs = stmt.executeQuery()) {
                int rowNum = 1;
                while (rs.next()) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(rs.getString("repository_name"));
                    row.createCell(1).setCellValue(rs.getString("repository_path"));
                    row.createCell(2).setCellValue(rs.getInt("total_test_classes"));
                    row.createCell(3).setCellValue(rs.getInt("total_test_methods"));
                    row.createCell(4).setCellValue(rs.getInt("total_annotated_methods"));
                    row.createCell(5).setCellValue(rs.getDouble("annotation_coverage_rate"));
                    row.createCell(6).setCellValue(rs.getTimestamp("last_scan_date").toString());
                }
            }
        }
        
        // Create repository chart
        createRepositoryChart(workbook, sheet);
    }
    
    /**
     * Create trends and analysis sheet
     */
    private static void createTrendsSheet(Workbook workbook, Sheet sheet) throws SQLException {
        // Set column widths
        sheet.setColumnWidth(0, 3000);
        sheet.setColumnWidth(1, 3000);
        sheet.setColumnWidth(2, 3000);
        sheet.setColumnWidth(3, 3000);
        sheet.setColumnWidth(4, 3000);
        
        // Create headers
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Date", "Repositories", "Test Classes", "Test Methods", "Coverage %"};
        
        CellStyle headerStyle = createHeaderStyle(workbook);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Get trend data (last 30 days)
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM daily_metrics WHERE metric_date >= CURRENT_DATE - INTERVAL '30 days' " +
                 "ORDER BY metric_date DESC")) {
            
            try (ResultSet rs = stmt.executeQuery()) {
                int rowNum = 1;
                while (rs.next()) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(rs.getDate("metric_date").toString());
                    row.createCell(1).setCellValue(rs.getInt("total_repositories"));
                    row.createCell(2).setCellValue(rs.getInt("total_test_classes"));
                    row.createCell(3).setCellValue(rs.getInt("total_test_methods"));
                    row.createCell(4).setCellValue(rs.getDouble("overall_coverage_rate"));
                }
            }
        }
        
        // Create trends chart
        createTrendsChart(workbook, sheet);
    }
    
    
        /**
     * Create annotation coverage analysis sheet
     */
    private static void createCoverageSheet(Workbook workbook, Sheet sheet) throws SQLException {
        // Set column widths
        sheet.setColumnWidth(0, 4000);
        sheet.setColumnWidth(1, 3000);
        sheet.setColumnWidth(2, 3000);
        sheet.setColumnWidth(3, 3000);
        sheet.setColumnWidth(4, 4000);
        
        // Create headers
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Repository", "Test Classes", "Coverage %", "Status", "Recommendations"};
        
        CellStyle headerStyle = createHeaderStyle(workbook);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Get coverage data
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT repository_name, total_test_classes, annotation_coverage_rate " +
                 "FROM repositories ORDER BY annotation_coverage_rate ASC")) {
            
            try (ResultSet rs = stmt.executeQuery()) {
                int rowNum = 1;
                while (rs.next()) {
                    Row row = sheet.createRow(rowNum++);
                    String repoName = rs.getString("repository_name");
                    double coverage = rs.getDouble("annotation_coverage_rate");
                    
                    row.createCell(0).setCellValue(repoName);
                    row.createCell(1).setCellValue(rs.getInt("total_test_classes"));
                    row.createCell(2).setCellValue(coverage);
                    
                    // Set status and recommendations
                    if (coverage >= 80) {
                        row.createCell(3).setCellValue("Excellent");
                        row.createCell(4).setCellValue("Maintain current standards");
                    } else if (coverage >= 60) {
                        row.createCell(3).setCellValue("Good");
                        row.createCell(4).setCellValue("Focus on remaining test methods");
                    } else if (coverage >= 40) {
                        row.createCell(3).setCellValue("Fair");
                        row.createCell(4).setCellValue("Prioritize high-impact test methods");
                    } else {
                        row.createCell(3).setCellValue("Needs Improvement");
                        row.createCell(4).setCellValue("Immediate attention required");
                    }
                }
            }
        }
        
        // Create coverage chart
        createCoverageChart(workbook, sheet);
    }
    
    /**
     * Create test method details sheet with comprehensive annotation information
     */
    private static void createTestMethodDetailsSheet(Workbook workbook, Sheet sheet) throws SQLException {
        // Set column widths for comprehensive test method information
        sheet.setColumnWidth(0, 3000);  // Repository
        sheet.setColumnWidth(1, 4000);  // Test Class
        sheet.setColumnWidth(2, 4000);  // Test Method
        sheet.setColumnWidth(3, 3000);  // Line Number
        sheet.setColumnWidth(4, 4000);  // Title
        sheet.setColumnWidth(5, 3000);  // Author
        sheet.setColumnWidth(6, 3000);  // Status
        sheet.setColumnWidth(7, 4000);  // Target Class
        sheet.setColumnWidth(8, 4000);  // Target Method
        sheet.setColumnWidth(9, 6000);  // Description
        sheet.setColumnWidth(10, 4000); // Test Points
        sheet.setColumnWidth(11, 4000); // Tags
        sheet.setColumnWidth(12, 4000); // Requirements
        sheet.setColumnWidth(13, 4000); // Test Case IDs
        sheet.setColumnWidth(14, 4000); // Defects
        sheet.setColumnWidth(15, 4000); // Last Update
        sheet.setColumnWidth(16, 3000); // Last Update Author
        
        // Create headers
        Row headerRow = sheet.createRow(0);
        String[] headers = {
            "Repository", "Test Class", "Test Method", "Line", "Title", "Author", "Status",
            "Target Class", "Target Method", "Description", "Test Points", "Tags", 
            "Requirements", "Test Case IDs", "Defects", "Last Update", "Last Update Author"
        };
        
        CellStyle headerStyle = createHeaderStyle(workbook);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Get comprehensive test method data with annotations
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT " +
                 "    r.repository_name, " +
                 "    tc.class_name, " +
                 "    tc.package_name, " +
                 "    tm.method_name, " +
                 "    tm.line_number, " +
                 "    tm.annotation_title, " +
                 "    tm.annotation_author, " +
                 "    tm.annotation_status, " +
                 "    tm.annotation_target_class, " +
                 "    tm.annotation_target_method, " +
                 "    tm.annotation_description, " +
                 "    tm.annotation_test_points, " +
                 "    tm.annotation_tags, " +
                 "    tm.annotation_requirements, " +
                 "    tm.annotation_data, " +
                 "    tm.last_modified_date " +
                 "FROM test_methods tm " +
                 "JOIN test_classes tc ON tm.test_class_id = tc.id " +
                 "JOIN repositories r ON tc.repository_id = r.id " +
                 "WHERE tm.has_annotation = true " +
                 "ORDER BY r.repository_name, tc.class_name, tm.method_name")) {
            
            try (ResultSet rs = stmt.executeQuery()) {
                int rowNum = 1;
                while (rs.next()) {
                    Row row = sheet.createRow(rowNum++);
                    
                    // Repository and class information
                    row.createCell(0).setCellValue(rs.getString("repository_name"));
                    row.createCell(1).setCellValue(rs.getString("class_name"));
                    row.createCell(2).setCellValue(rs.getString("method_name"));
                    row.createCell(3).setCellValue(rs.getInt("line_number"));
                    
                    // Annotation information
                    row.createCell(4).setCellValue(rs.getString("annotation_title"));
                    row.createCell(5).setCellValue(rs.getString("annotation_author"));
                    row.createCell(6).setCellValue(rs.getString("annotation_status"));
                    row.createCell(7).setCellValue(rs.getString("annotation_target_class"));
                    row.createCell(8).setCellValue(rs.getString("annotation_target_method"));
                    row.createCell(9).setCellValue(rs.getString("annotation_description"));
                    
                    // Array fields - convert to comma-separated strings
                    String[] testPoints = (String[]) rs.getArray("annotation_test_points").getArray();
                    row.createCell(10).setCellValue(arrayToString(testPoints));
                    
                    String[] tags = (String[]) rs.getArray("annotation_tags").getArray();
                    row.createCell(11).setCellValue(arrayToString(tags));
                    
                    String[] requirements = (String[]) rs.getArray("annotation_requirements").getArray();
                    row.createCell(12).setCellValue(arrayToString(requirements));
                    
                    // Extract test case IDs from JSONB data if available
                    String testCaseIds = extractTestCaseIdsFromJson(rs.getString("annotation_data"));
                    row.createCell(13).setCellValue(testCaseIds);
                    
                    // Extract defects from JSONB data if available
                    String defects = extractDefectsFromJson(rs.getString("annotation_data"));
                    row.createCell(14).setCellValue(defects);
                    
                    // Timestamp information
                    if (rs.getTimestamp("last_modified_date") != null) {
                        row.createCell(15).setCellValue(rs.getTimestamp("last_modified_date").toString());
                    }
                    
                    // Last update author from JSONB data if available
                    String lastUpdateAuthor = extractLastUpdateAuthorFromJson(rs.getString("annotation_data"));
                    row.createCell(16).setCellValue(lastUpdateAuthor);
                }
            }
        }
        
        // Create summary note
        createTestMethodSummaryNote(workbook, sheet);
    }
    
    /**
     * Helper method to convert array to comma-separated string
     */
    private static String arrayToString(String[] array) {
        if (array == null || array.length == 0) {
            return "";
        }
        return String.join(", ", array);
    }
    
    /**
     * Helper method to extract test case IDs from JSONB data
     */
    private static String extractTestCaseIdsFromJson(String jsonData) {
        if (jsonData == null || jsonData.isEmpty()) {
            return "";
        }
        
        try {
            // Simple JSON parsing to extract relatedTestcases
            if (jsonData.contains("\"relatedTestcases\"")) {
                int start = jsonData.indexOf("\"relatedTestcases\"");
                int arrayStart = jsonData.indexOf("[", start);
                if (arrayStart != -1) {
                    int arrayEnd = jsonData.indexOf("]", arrayStart);
                    if (arrayEnd != -1) {
                        String testCasesStr = jsonData.substring(arrayStart + 1, arrayEnd);
                        // Remove quotes and split by comma
                        return testCasesStr.replaceAll("\"", "").trim();
                    }
                }
            }
        } catch (Exception e) {
            // If JSON parsing fails, return empty string
        }
        
        return "";
    }
    
    /**
     * Helper method to extract defects from JSONB data
     */
    private static String extractDefectsFromJson(String jsonData) {
        if (jsonData == null || jsonData.isEmpty()) {
            return "";
        }
        
        try {
            // Simple JSON parsing to extract relatedDefects
            if (jsonData.contains("\"relatedDefects\"")) {
                int start = jsonData.indexOf("\"relatedDefects\"");
                int arrayStart = jsonData.indexOf("[", start);
                if (arrayStart != -1) {
                    int arrayEnd = jsonData.indexOf("]", arrayStart);
                    if (arrayEnd != -1) {
                        String defectsStr = jsonData.substring(arrayStart + 1, arrayEnd);
                        // Remove quotes and split by comma
                        return defectsStr.replaceAll("\"", "").trim();
                    }
                }
            }
        } catch (Exception e) {
            // If JSON parsing fails, return empty string
        }
        
        return "";
    }
    
    /**
     * Helper method to extract last update author from JSONB data
     */
    private static String extractLastUpdateAuthorFromJson(String jsonData) {
        if (jsonData == null || jsonData.isEmpty()) {
            return "";
        }
        
        try {
            // Simple JSON parsing to extract lastUpdateAuthor
            if (jsonData.contains("\"lastUpdateAuthor\"")) {
                int start = jsonData.indexOf("\"lastUpdateAuthor\"");
                int valueStart = jsonData.indexOf("\"", start + 18);
                if (valueStart != -1) {
                    int valueEnd = jsonData.indexOf("\"", valueStart + 1);
                    if (valueEnd != -1) {
                        return jsonData.substring(valueStart + 1, valueEnd);
                    }
                }
            }
        } catch (Exception e) {
            // If JSON parsing fails, return empty string
        }
        
        return "";
    }
    
    /**
     * Create summary note for test method details sheet
     */
    private static void createTestMethodSummaryNote(Workbook workbook, Sheet sheet) {
        Row noteRow = sheet.createRow(sheet.getLastRowNum() + 2);
        Cell noteCell = noteRow.createCell(0);
        noteCell.setCellValue("📋 Test Method Details - Review Guide");
        
        CellStyle noteStyle = workbook.createCellStyle();
        Font noteFont = workbook.createFont();
        noteFont.setBold(true);
        noteFont.setFontHeightInPoints((short) 12);
        noteStyle.setFont(noteFont);
        noteCell.setCellStyle(noteStyle);
        
        // Add review instructions
        Row instructionRow1 = sheet.createRow(sheet.getLastRowNum() + 1);
        Cell instructionCell1 = instructionRow1.createCell(0);
        instructionCell1.setCellValue("• Review Test Case IDs to ensure they match actual test case descriptions");
        instructionCell1.setCellStyle(createInstructionStyle(workbook));
        
        Row instructionRow2 = sheet.createRow(sheet.getLastRowNum() + 1);
        Cell instructionCell2 = instructionRow2.createCell(0);
        instructionCell2.setCellValue("• Verify test descriptions accurately reflect what is being tested");
        instructionCell2.setCellStyle(createInstructionStyle(workbook));
        
        Row instructionRow3 = sheet.createRow(sheet.getLastRowNum() + 1);
        Cell instructionCell3 = instructionRow3.createCell(0);
        instructionCell3.setCellValue("• Check that test points and requirements are properly linked");
        instructionCell3.setCellStyle(createInstructionStyle(workbook));
        
        Row instructionRow4 = sheet.createRow(sheet.getLastRowNum() + 1);
        Cell instructionCell4 = instructionRow4.createCell(0);
        instructionCell4.setCellValue("• Ensure test status reflects current implementation state");
        instructionCell4.setCellStyle(createInstructionStyle(workbook));
    }
    
    /**
     * Create instruction style for the review guide
     */
    private static CellStyle createInstructionStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setItalic(true);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        return style;
    }
    
    /**
     * Create helper methods for styling and data
     */
    private static void createInfoRow(Sheet sheet, int rowNum, String label, Object value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value.toString());
    }
    
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }
    
    /**
     * Create summary chart (placeholder for now)
     */
    private static void createSummaryChart(Workbook workbook, Sheet sheet, int startRow) {
        // This would create a chart using Apache POI's chart API
        // For now, we'll just add a note
        Row chartRow = sheet.createRow(startRow);
        Cell chartCell = chartRow.createCell(0);
        chartCell.setCellValue("Summary Chart - Coverage Overview");
        
        CellStyle noteStyle = workbook.createCellStyle();
        Font noteFont = workbook.createFont();
        noteFont.setItalic(true);
        noteStyle.setFont(noteFont);
        chartCell.setCellStyle(noteStyle);
    }
    
    /**
     * Create repository chart (placeholder)
     */
    private static void createRepositoryChart(Workbook workbook, Sheet sheet) {
        Row chartRow = sheet.createRow(sheet.getLastRowNum() + 2);
        Cell chartCell = chartRow.createCell(0);
        chartCell.setCellValue("Repository Coverage Chart - Top 10 Repositories");
        
        CellStyle noteStyle = workbook.createCellStyle();
        Font noteFont = workbook.createFont();
        noteFont.setItalic(true);
        noteStyle.setFont(noteFont);
        chartCell.setCellStyle(noteStyle);
    }
    
    /**
     * Create trends chart (placeholder)
     */
    private static void createTrendsChart(Workbook workbook, Sheet sheet) {
        Row chartRow = sheet.createRow(sheet.getLastRowNum() + 2);
        Cell chartCell = chartRow.createCell(0);
        chartCell.setCellValue("Trends Chart - 30-Day Coverage Trends");
        
        CellStyle noteStyle = workbook.createCellStyle();
        Font noteFont = workbook.createFont();
        noteFont.setItalic(true);
        noteStyle.setFont(noteFont);
        chartCell.setCellStyle(noteStyle);
    }
    
    /**
     * Create coverage chart (placeholder)
     */
    private static void createCoverageChart(Workbook workbook, Sheet sheet) {
        Row chartRow = sheet.createRow(sheet.getLastRowNum() + 2);
        Cell chartCell = chartRow.createCell(0);
        chartCell.setCellValue("Coverage Analysis Chart - Repository Performance");
        
        CellStyle noteStyle = workbook.createCellStyle();
        Font noteFont = workbook.createFont();
        noteFont.setItalic(true);
        noteStyle.setFont(noteFont);
        chartCell.setCellStyle(noteStyle);
    }
}
