package com.example.annotationextractor;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Scanner class to find Java git repositories and their test directories
 * with support for path pattern 
 * 
 */

public class RepositoryScanner {
    
    /**
     * Scan a directory for Java git repositories and collect test information
     * 
     * @param rootDirectory Path to the directory containing git repositories
     * @return TestCollectionSummary containing all collected test information
     * @throws IOException if directory cannot be scanned
     */
    public static TestCollectionSummary scanRepositories(String rootDirectory) throws IOException {
        return scanRepositories(rootDirectory, null, null);
    }
    
    /**
     * Scan a directory for Java git repositories with path pattern filtering
     * 
     * @param rootDirectory Path to the directory containing git repositories
     * @param includePatterns List of path patterns to include )
     * @param excludePatterns List of path patterns to exclude
     * @return TestCollectionSummary containing all collected test information
     * @throws IOException if directory cannot be scanned
     */
    public static TestCollectionSummary scanRepositories(String rootDirectory, List<String> includePatterns, List<String> excludePatterns) throws IOException {
        Path rootPath = Paths.get(rootDirectory);
        if (!Files.exists(rootPath) || !Files.isDirectory(rootPath)) {
            throw new IOException("Directory does not exist or is not a directory: " + rootDirectory);
        }
        
        TestCollectionSummary summary = new TestCollectionSummary(rootDirectory);
        
        // Find all git repositories in the root directory
        List<Path> gitRepositories = findGitRepositories(rootPath);
        
        for (Path repoPath : gitRepositories) {
            try {
                // Check if repository path matches include/exclude patterns
                if (shouldIncludeRepository(repoPath, rootPath, includePatterns, excludePatterns)) {
                    RepositoryTestInfo repoInfo = scanRepository(repoPath);
                    if (repoInfo.getTotalTestClasses() > 0) {
                        summary.addRepository(repoInfo);
                    }
                } else {
                    System.out.println("Skipping repository (pattern filter): " + repoPath);
                }
            } catch (Exception e) {
                System.err.println("Error scanning repository " + repoPath + ": " + e.getMessage());
                // Continue with other repositories
            }
        }
        
        return summary;
    }
    
    /**
     * Check if a repository should be included based on path patterns
     */
    private static boolean shouldIncludeRepository(Path repoPath, Path rootPath, List<String> includePatterns, List<String> excludePatterns) {
        // If no patterns specified, include everything
        if ((includePatterns == null || includePatterns.isEmpty()) && 
            (excludePatterns == null || excludePatterns.isEmpty())) {
            return true;
        }
        
        // Convert repository path to relative path from root
        String relativePath = rootPath.relativize(repoPath).toString().replace('\\', '/');
        
        // Check exclude patterns first (exclusions take precedence)
        if (excludePatterns != null) {
            for (String excludePattern : excludePatterns) {
                if (matchesPattern(relativePath, excludePattern)) {
                    return false; // Excluded
                }
            }
        }
        
        // Check include patterns
        if (includePatterns != null && !includePatterns.isEmpty()) {
            for (String includePattern : includePatterns) {
                if (matchesPattern(relativePath, includePattern)) {
                    return true; // Included
                }
            }
            return false; // No include pattern matched
        }
        
        return true; // No include patterns specified, so include everything not excluded
    }
    
    /**
     * Check if a path matches a pattern
     * Supports glob-like patterns with * and ** wildcards
     */
    private static boolean matchesPattern(String path, String pattern) {
        // Convert glob pattern to regex
        String regexPattern = convertGlobToRegex(pattern);
        Pattern compiledPattern = Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = compiledPattern.matcher(path);
        return matcher.matches();
    }
    
    /**
     * Convert glob pattern to regex pattern
     * Supports:
     * * - matches any sequence of characters except path separators
     * ** - matches any sequence of characters including path separators
     * ? - matches any single character except path separators
     */
    private static String convertGlobToRegex(String globPattern) {
        StringBuilder regex = new StringBuilder();
        regex.append("^");
        
        for (int i = 0; i < globPattern.length(); i++) {
            char c = globPattern.charAt(i);
            switch (c) {
                case '*':
                    if (i + 1 < globPattern.length() && globPattern.charAt(i + 1) == '*') {
                        // ** - matches any sequence including path separators
                        regex.append(".*");
                        i++; // Skip next *
                    } else {
                        // * - matches any sequence except path separators
                        regex.append("[^/]*");
                    }
                    break;
                case '?':
                    // ? - matches any single character except path separators
                    regex.append("[^/]");
                    break;
                case '.':
                    // Escape dot
                    regex.append("\\.");
                    break;
                case '(':
                case ')':
                case '[':
                case ']':
                case '{':
                case '}':
                case '+':
                case '|':
                case '^':
                case '$':
                case '\\':
                    // Escape regex special characters
                    regex.append("\\").append(c);
                    break;
                default:
                    regex.append(c);
            }
        }
        
        regex.append("$");
        return regex.toString();
    }
    
    /**
     * Find all git repositories in a directory
     */
    private static List<Path> findGitRepositories(Path rootPath) throws IOException {
        List<Path> repositories = new ArrayList<>();
        
        Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                // Check if this directory contains a .git folder
                if (Files.exists(dir.resolve(".git"))) {
                    repositories.add(dir);
                    return FileVisitResult.SKIP_SUBTREE; // Don't go deeper into this repo
                }
                return FileVisitResult.CONTINUE;
            }
            
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                // Log error but continue
                System.err.println("Failed to visit file: " + file + " - " + exc.getMessage());
                return FileVisitResult.CONTINUE;
            }
        });
        
        return repositories;
    }
    
    /**
     * Scan a single repository for test classes
     */
    private static RepositoryTestInfo scanRepository(Path repoPath) throws IOException {
        String repoName = repoPath.getFileName().toString();
        RepositoryTestInfo repoInfo = new RepositoryTestInfo(repoName, repoPath.toString());
        
        // Try to extract git URL from the repository
        String gitUrl = extractGitUrlFromRepository(repoPath);
        if (gitUrl != null) {
            repoInfo.setGitUrl(gitUrl);
        }
        
        // Find test directories following standard Java conventions
        List<Path> testDirectories = findTestDirectories(repoPath);
        
        for (Path testDir : testDirectories) {
            List<Path> javaFiles = findJavaFiles(testDir);
            
            for (Path javaFile : javaFiles) {
                try {
                    TestClassInfo testClassInfo = TestClassParser.parseTestClass(javaFile);
                    if (testClassInfo.getTotalTestMethods() > 0) {
                        repoInfo.addTestClass(testClassInfo);
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing test class " + javaFile + ": " + e.getMessage());
                    // Continue with other files
                }
            }
        }
        
        return repoInfo;
    }
    
    /**
     * Extract git URL from a repository directory
     */
    private static String extractGitUrlFromRepository(Path repoPath) {
        try {
            Path gitConfigPath = repoPath.resolve(".git").resolve("config");
            if (Files.exists(gitConfigPath)) {
                List<String> lines = Files.readAllLines(gitConfigPath);
                for (String line : lines) {
                    line = line.trim();
                    if (line.startsWith("url = ")) {
                        return line.substring(6).trim();
                    }
                }
            }
        } catch (IOException e) {
            // Ignore errors reading git config
        }
        return null;
    }
    
    /**
     * Find test directories following standard Java conventions
     */
    private static List<Path> findTestDirectories(Path repoPath) throws IOException {
        List<Path> testDirs = new ArrayList<>();
        
        // Common test directory patterns
        String[] testDirPatterns = {
            "src/test/java",
            "src/test",
            "test",
            "tests",
            "test/java",
            "tests/java"
        };
        
        for (String pattern : testDirPatterns) {
            Path testDir = repoPath.resolve(pattern);
            if (Files.exists(testDir) && Files.isDirectory(testDir)) {
                testDirs.add(testDir);
            }
        }
        
        // Also look for test directories in src subdirectories
        try {
            Files.walkFileTree(repoPath.resolve("src"), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (dir.getFileName().toString().equals("test")) {
                        testDirs.add(dir);
                    }
                    return FileVisitResult.CONTINUE;
                }
                
                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            // src directory might not exist, continue
        }
        
        return testDirs;
    }
    
    /**
     * Find all Java files in a directory recursively
     */
    private static List<Path> findJavaFiles(Path directory) throws IOException {
        List<Path> javaFiles = new ArrayList<>();
        
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.toString().endsWith(".java")) {
                    javaFiles.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
            
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                return FileVisitResult.CONTINUE;
            }
        });
        
        return javaFiles;
    }
}
