package com.example.annotationextractor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Processes a text file containing a list of git repository URLs
 * Filters out comments and empty lines
 */
public class RepositoryListProcessor {
    
    /**
     * Read repository URLs from a text file
     * 
     * @param filePath Path to the text file
     * @return List of git repository URLs
     * @throws IOException if file cannot be read
     */
    public static List<String> readRepositoryUrls(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("Repository list file not found: " + filePath);
        }
        
        List<String> urls = new ArrayList<>();
        List<String> lines = Files.readAllLines(path);
        
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            
            // Skip empty lines and comments
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            
            // Validate that it looks like a git URL
            if (isValidGitUrl(line)) {
                urls.add(line);
            } else {
                System.err.println("Warning: Line " + (i + 1) + " does not appear to be a valid git URL: " + line);
            }
        }
        
        return urls;
    }
    
    /**
     * Read repository URLs from a text file with filtering options
     * 
     * @param filePath Path to the text file
     * @param includePatterns List of patterns to include (can be null)
     * @param excludePatterns List of patterns to exclude (can be null)
     * @return List of filtered git repository URLs
     * @throws IOException if file cannot be read
     */
    public static List<String> readRepositoryUrls(String filePath, List<String> includePatterns, List<String> excludePatterns) throws IOException {
        List<String> allUrls = readRepositoryUrls(filePath);
        
        if ((includePatterns == null || includePatterns.isEmpty()) && 
            (excludePatterns == null || excludePatterns.isEmpty())) {
            return allUrls;
        }
        
        return allUrls.stream()
            .filter(url -> shouldIncludeUrl(url, includePatterns, excludePatterns))
            .collect(Collectors.toList());
    }
    
    /**
     * Check if a URL should be included based on patterns
     */
    private static boolean shouldIncludeUrl(String url, List<String> includePatterns, List<String> excludePatterns) {
        // Check exclude patterns first (exclusions take precedence)
        if (excludePatterns != null) {
            for (String excludePattern : excludePatterns) {
                if (url.matches(excludePattern.replace("*", ".*"))) {
                    return false; // Excluded
                }
            }
        }
        
        // Check include patterns
        if (includePatterns != null && !includePatterns.isEmpty()) {
            for (String includePattern : includePatterns) {
                if (url.matches(includePattern.replace("*", ".*"))) {
                    return true; // Included
                }
            }
            return false; // No include pattern matched
        }
        
        return true; // No include patterns specified, so include everything not excluded
    }
    
    /**
     * Basic validation that a string looks like a git URL
     */
    private static boolean isValidGitUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        
        // Check for common git URL patterns
        return url.startsWith("https://") || 
               url.startsWith("http://") || 
               url.startsWith("git://") || 
               url.startsWith("ssh://") ||
               url.startsWith("git@") ||
               url.endsWith(".git") ||
               url.contains("github.com") ||
               url.contains("gitlab.com") ||
               url.contains("bitbucket.org");
    }
    
    /**
     * Create a sample repository list file
     * 
     * @param filePath Path where to create the sample file
     * @throws IOException if file cannot be created
     */
    public static void createSampleRepositoryList(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        
        List<String> sampleUrls = List.of(
            "# Sample Repository List",
            "# Add your git repository URLs here, one per line",
            "# Lines starting with # are comments and will be ignored",
            "",
            "# Example repositories:",
            "https://github.com/example/repo1.git",
            "https://github.com/example/repo2",
            "git@github.com:example/repo3.git",
            "",
            "# You can also use patterns for filtering:",
            "# Include only repositories with 'test' in the name:",
            "# include: *test*",
            "# Exclude repositories with 'legacy' in the name:",
            "# exclude: *legacy*"
        );
        
        Files.write(path, sampleUrls);
        System.out.println("Sample repository list file created: " + filePath);
    }
    
    /**
     * Get statistics about the repository list file
     * 
     * @param filePath Path to the text file
     * @return String containing statistics
     * @throws IOException if file cannot be read
     */
    public static String getFileStatistics(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return "File not found: " + filePath;
        }
        
        List<String> lines = Files.readAllLines(path);
        int totalLines = lines.size();
        int emptyLines = 0;
        int commentLines = 0;
        int urlLines = 0;
        int invalidLines = 0;
        
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                emptyLines++;
            } else if (trimmed.startsWith("#")) {
                commentLines++;
            } else if (isValidGitUrl(trimmed)) {
                urlLines++;
            } else {
                invalidLines++;
            }
        }
        
        return String.format(
            "Repository List File Statistics:\n" +
            "  Total lines: %d\n" +
            "  Empty lines: %d\n" +
            "  Comment lines: %d\n" +
            "  Valid URLs: %d\n" +
            "  Invalid lines: %d",
            totalLines, emptyLines, commentLines, urlLines, invalidLines
        );
    }
}
