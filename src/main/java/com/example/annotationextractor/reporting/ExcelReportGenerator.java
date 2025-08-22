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
