package com.example.annotationextractor.service;

import com.example.annotationextractor.web.dto.ExportRequestDto;
import com.example.annotationextractor.web.dto.ExportStatusDto;
import com.example.annotationextractor.web.dto.TestMethodDetailDto;
import com.example.annotationextractor.web.dto.RepositoryMetricsDto;
import com.example.annotationextractor.web.dto.TeamMetricsDto;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service for handling server-side data exports
 * Implements industry best practices for large dataset exports
 */
@Service
public class ExportService {

    private final RepositoryDataService repositoryDataService;
    private final TeamDataService teamDataService;
    private final ExecutorService executorService;
    private final Map<String, ExportStatusDto> exportJobs;
    private final Map<String, CompletableFuture<Void>> runningJobs;
    private final String exportDirectory;

    public ExportService(RepositoryDataService repositoryDataService, TeamDataService teamDataService) {
        this.repositoryDataService = repositoryDataService;
        this.teamDataService = teamDataService;
        this.executorService = Executors.newFixedThreadPool(4);
        this.exportJobs = new ConcurrentHashMap<>();
        this.runningJobs = new ConcurrentHashMap<>();
        
        // Create export directory
        this.exportDirectory = System.getProperty("java.io.tmpdir") + "/testcraft-exports/";
        try {
            Files.createDirectories(Paths.get(exportDirectory));
        } catch (IOException e) {
            System.err.println("Failed to create export directory: " + e.getMessage());
        }
        
        System.out.println("ExportService initialized successfully");
    }

    /**
     * Initiate a new export job
     */
    public ExportStatusDto initiateExport(String jobId, ExportRequestDto request) {
        ExportStatusDto status = new ExportStatusDto(jobId, "pending", 0, "Export job queued");
        exportJobs.put(jobId, status);

        // Start export job asynchronously
        CompletableFuture<Void> job = CompletableFuture.runAsync(() -> {
            processExport(jobId, request);
        }, executorService);

        runningJobs.put(jobId, job);

        return status;
    }

    /**
     * Process the actual export
     */
    private void processExport(String jobId, ExportRequestDto request) {
        try {
            updateStatus(jobId, "processing", 10, "Starting export...");

            // Determine total records for progress tracking
            long totalRecords = getTotalRecordCount(request);
            updateStatus(jobId, "processing", 20, "Found " + totalRecords + " records to export");

            // Generate filename if not provided
            String filename = request.getFilename();
            if (filename == null || filename.trim().isEmpty()) {
                filename = generateFilename(request);
            }

            // Process export based on data type
            switch (request.getDataType()) {
                case "test-methods":
                    exportTestMethodDetails(jobId, request, totalRecords);
                    break;
                case "repositories":
                    exportRepositories(jobId, request, totalRecords);
                    break;
                case "teams":
                    exportTeams(jobId, request, totalRecords);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported data type: " + request.getDataType());
            }

            // Update final status
            ExportStatusDto status = exportJobs.get(jobId);
            status.setStatus("completed");
            status.setProgress(100);
            status.setMessage("Export completed successfully");
            status.setCompletedAt(LocalDateTime.now());
            status.setFilename(filename);
            status.setDownloadUrl("/export/download/" + jobId);
            status.setTotalRecords(totalRecords);
            status.setProcessedRecords(totalRecords);

            // Clean up running job
            runningJobs.remove(jobId);

        } catch (Exception e) {
            System.err.println("Export job " + jobId + " failed: " + e.getMessage());
            e.printStackTrace();
            
            ExportStatusDto status = exportJobs.get(jobId);
            if (status != null) {
                status.setStatus("failed");
                status.setErrorMessage(e.getMessage());
                status.setCompletedAt(LocalDateTime.now());
            }
            
            runningJobs.remove(jobId);
        }
    }

    /**
     * Export test method details
     */
    private String exportTestMethodDetails(String jobId, ExportRequestDto request, long totalRecords) throws IOException {
        updateStatus(jobId, "processing", 30, "Fetching test method data...");

        String filename = request.getFilename() != null ? request.getFilename() : generateFilename(request);
        String filePath = exportDirectory + filename;

        // Extract filters
        Map<String, Object> filters = request.getFilters() != null ? request.getFilters() : new HashMap<>();
        String organization = (String) filters.get("organization");
        String teamName = (String) filters.get("teamName");
        String repositoryName = (String) filters.get("repositoryName");
        String packageName = (String) filters.get("packageName");
        String className = (String) filters.get("className");
        Boolean annotated = (Boolean) filters.get("annotated");

        try (FileWriter writer = new FileWriter(filePath);
             PrintWriter printWriter = new PrintWriter(writer)) {

            // Write CSV header
            printWriter.println("ID,Repository,Test Class,Test Method,Line,Title,Author,Status,Target Class,Target Method,Description,Test Points,Tags,Requirements,Test Case IDs,Defects,Last Modified,Last Update Author,Team Name,Team Code,Git URL");

            // Process data in chunks to avoid memory issues
            int pageSize = 1000; // Process 1000 records at a time
            int page = 0;
            long processedRecords = 0;

            while (processedRecords < totalRecords) {
                updateStatus(jobId, "processing", 
                    30 + (int) ((processedRecords * 60.0) / totalRecords), 
                    "Processing records " + processedRecords + " to " + Math.min(processedRecords + pageSize, totalRecords));

                // Fetch chunk of data with all filter parameters
                com.example.annotationextractor.web.dto.PagedResponse<TestMethodDetailDto> chunkData = 
                    repositoryDataService.getTestMethodDetailsPaginated(
                        page, pageSize, organization, teamName, repositoryName, packageName, className, annotated, null);
                
                if (chunkData.getContent().isEmpty()) {
                    break; // No more data
                }

                // Write chunk to file
                for (TestMethodDetailDto method : chunkData.getContent()) {
                    writeTestMethodToCsv(printWriter, method);
                    processedRecords++;
                }

                page++;
            }

            updateStatus(jobId, "processing", 90, "Finalizing export file...");
        }

        return filePath;
    }

    /**
     * Write a single test method to CSV
     */
    private void writeTestMethodToCsv(PrintWriter writer, TestMethodDetailDto method) {
        writer.printf("%d,%s,%s,%s,%d,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
            method.getId(),
            escapeCsv(method.getRepository()),
            escapeCsv(method.getTestClass()),
            escapeCsv(method.getTestMethod()),
            method.getLine(),
            escapeCsv(method.getTitle()),
            escapeCsv(method.getAuthor()),
            escapeCsv(method.getStatus()),
            escapeCsv(method.getTargetClass()),
            escapeCsv(method.getTargetMethod()),
            escapeCsv(method.getDescription()),
            escapeCsv(method.getTestPoints()),
            escapeCsv(String.join(";", method.getTags())),
            escapeCsv(String.join(";", method.getRequirements())),
            escapeCsv(String.join(";", method.getTestCaseIds())),
            escapeCsv(String.join(";", method.getDefects())),
            escapeCsv(method.getLastModified() != null ? method.getLastModified().toString() : null),
            escapeCsv(method.getLastUpdateAuthor()),
            escapeCsv(method.getTeamName()),
            escapeCsv(method.getTeamCode()),
            escapeCsv(method.getGitUrl())
        );
    }

    /**
     * Escape CSV values
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        // Escape quotes and wrap in quotes if contains comma, quote, or newline
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Export repositories
     */
    private String exportRepositories(String jobId, ExportRequestDto request, long totalRecords) throws IOException {
        updateStatus(jobId, "processing", 30, "Fetching repository data...");

        String filename = request.getFilename() != null ? request.getFilename() : generateFilename(request);
        String filePath = exportDirectory + filename;

        try (FileWriter writer = new FileWriter(filePath);
             PrintWriter printWriter = new PrintWriter(writer)) {

            // Write CSV header
            printWriter.println("ID,Name,Git URL,Team Name,Team Code,Total Classes,Total Methods,Annotated Methods,Coverage Rate,Last Scan Date");

            // Get repository data
            List<RepositoryMetricsDto> repositories = repositoryDataService.getAllRepositoryMetrics();
            
            updateStatus(jobId, "processing", 50, "Processing " + repositories.size() + " repositories...");

            // Write repository data
            for (RepositoryMetricsDto repo : repositories) {
                writeRepositoryToCsv(printWriter, repo);
            }

            updateStatus(jobId, "processing", 90, "Finalizing export file...");
        }

        return filePath;
    }

    /**
     * Write a single repository to CSV
     */
    private void writeRepositoryToCsv(PrintWriter writer, RepositoryMetricsDto repo) {
        writer.printf("%d,%s,%s,%s,%s,%d,%d,%d,%.2f,%s%n",
            repo.getId(),
            escapeCsv(repo.getRepositoryName()),
            escapeCsv(repo.getGitUrl()),
            escapeCsv(repo.getTeamName()),
            escapeCsv(repo.getTechnologyStack()),
            repo.getTestClassCount(),
            repo.getTestMethodCount(),
            repo.getAnnotatedMethodCount(),
            repo.getCoverageRate(),
            escapeCsv(repo.getLastScanDate() != null ? repo.getLastScanDate().toString() : null)
        );
    }

    /**
     * Export teams
     */
    private String exportTeams(String jobId, ExportRequestDto request, long totalRecords) throws IOException {
        updateStatus(jobId, "processing", 30, "Fetching team data...");

        String filename = request.getFilename() != null ? request.getFilename() : generateFilename(request);
        String filePath = exportDirectory + filename;

        try (FileWriter writer = new FileWriter(filePath);
             PrintWriter printWriter = new PrintWriter(writer)) {

            // Write CSV header
            printWriter.println("ID,Name,Code,Department,Repository Count,Total Classes,Total Methods,Annotated Methods,Coverage Rate");

            // Get team data
            List<TeamMetricsDto> teams = teamDataService.getTeamMetrics();
            
            updateStatus(jobId, "processing", 50, "Processing " + teams.size() + " teams...");

            // Write team data
            for (TeamMetricsDto team : teams) {
                writeTeamToCsv(printWriter, team);
            }

            updateStatus(jobId, "processing", 90, "Finalizing export file...");
        }

        return filePath;
    }

    /**
     * Write a single team to CSV
     */
    private void writeTeamToCsv(PrintWriter writer, TeamMetricsDto team) {
        writer.printf("%d,%s,%s,%s,%d,%d,%d,%d,%.2f%n",
            team.getId(),
            escapeCsv(team.getTeamName()),
            escapeCsv(team.getTeamCode()),
            escapeCsv(team.getDepartment()),
            team.getRepositoryCount(),
            team.getTotalTestClasses(),
            team.getTotalTestMethods(),
            team.getTotalAnnotatedMethods(),
            team.getAverageCoverageRate()
        );
    }

    /**
     * Get total record count for progress tracking
     */
    private long getTotalRecordCount(ExportRequestDto request) {
        try {
            switch (request.getDataType()) {
                case "test-methods":
                    Map<String, Object> filters = request.getFilters() != null ? request.getFilters() : new HashMap<>();
                    String organization = (String) filters.get("organization");
                    String teamName = (String) filters.get("teamName");
                    String repositoryName = (String) filters.get("repositoryName");
                    String packageName = (String) filters.get("packageName");
                    String className = (String) filters.get("className");
                    Boolean annotated = (Boolean) filters.get("annotated");

                    // Get a small sample to determine total count
                    com.example.annotationextractor.web.dto.PagedResponse<TestMethodDetailDto> sample = 
                        repositoryDataService.getTestMethodDetailsPaginated(
                            0, 1, organization, teamName, repositoryName, packageName, className, annotated, null);
                    return sample.getTotalElements();
                    
                case "repositories":
                    List<RepositoryMetricsDto> repositories = repositoryDataService.getAllRepositoryMetrics();
                    return repositories.size();
                    
                case "teams":
                    List<TeamMetricsDto> teams = teamDataService.getTeamMetrics();
                    return teams.size();
                    
                default:
                    return 0;
            }
        } catch (Exception e) {
            System.err.println("Error getting total record count: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Generate filename based on request
     */
    private String generateFilename(ExportRequestDto request) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("%s_%s_%s.%s", 
            request.getDataType(), 
            request.getScope(), 
            timestamp, 
            request.getFormat());
    }

    /**
     * Update export status
     */
    private void updateStatus(String jobId, String status, int progress, String message) {
        ExportStatusDto exportStatus = exportJobs.get(jobId);
        if (exportStatus != null) {
            exportStatus.setStatus(status);
            exportStatus.setProgress(progress);
            exportStatus.setMessage(message);
        }
    }

    /**
     * Get export status
     */
    public ExportStatusDto getExportStatus(String jobId) {
        return exportJobs.get(jobId);
    }

    /**
     * Get export file
     */
    public Resource getExportFile(String jobId) {
        ExportStatusDto status = exportJobs.get(jobId);
        if (status != null && "completed".equals(status.getStatus())) {
            String filename = status.getFilename();
            if (filename != null) {
                Path filePath = Paths.get(exportDirectory + filename);
                if (Files.exists(filePath)) {
                    return new FileSystemResource(filePath);
                }
            }
        }
        return null;
    }

    /**
     * Get export filename
     */
    public String getExportFilename(String jobId) {
        ExportStatusDto status = exportJobs.get(jobId);
        return status != null ? status.getFilename() : null;
    }

    /**
     * Cancel export job
     */
    public boolean cancelExport(String jobId) {
        CompletableFuture<Void> job = runningJobs.get(jobId);
        if (job != null) {
            job.cancel(true);
            runningJobs.remove(jobId);
            
            ExportStatusDto status = exportJobs.get(jobId);
            if (status != null) {
                status.setStatus("cancelled");
                status.setCompletedAt(LocalDateTime.now());
            }
            return true;
        }
        return false;
    }

    /**
     * Clean up old export files
     */
    public void cleanupOldExports() {
        try {
            Path exportDir = Paths.get(exportDirectory);
            if (Files.exists(exportDir)) {
                Files.list(exportDir)
                    .filter(path -> {
                        try {
                            return Files.getLastModifiedTime(path).toInstant()
                                .isBefore(java.time.Instant.now().minusSeconds(3600)); // 1 hour old
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            System.out.println("Cleaned up old export file: " + path.getFileName());
                        } catch (IOException e) {
                            System.err.println("Failed to delete old export file: " + path.getFileName());
                        }
                    });
            }
        } catch (IOException e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }
}
