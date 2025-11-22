package com.example.annotationextractor.runner;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.example.annotationextractor.casemodel.ParseResult;
import com.example.annotationextractor.casemodel.RepositoryTestInfo;
import com.example.annotationextractor.casemodel.TestClassInfo;
import com.example.annotationextractor.casemodel.TestClassParser;
import com.example.annotationextractor.casemodel.TestCollectionSummary;
import com.example.annotationextractor.casemodel.TestHelperClassInfo;
import com.example.annotationextractor.domain.model.ScanRepositoryEntry;
import com.example.annotationextractor.util.GitRepositoryManager;

import java.util.regex.Matcher;


/**
 * Scanner class to find Java git repositories and their test directories
 * with support for path pattern 
 * 
 */

public class RepositoryScanner {

    private final Map<String, RepositoryTestInfo> repositoryInfos = new LinkedHashMap<>();
    private final TestCollectionSummary summary;
    private final GitRepositoryManager gitRepositoryManager;
    private final Path dependenciesDir;
    private final int maxRepositoriesPerScan;

    public RepositoryScanner(GitRepositoryManager gitRepositoryManager, List<ScanRepositoryEntry> repositoryEntries, int maxRepositoriesPerScan) throws IOException {
        this(gitRepositoryManager, repositoryEntries, maxRepositoriesPerScan, null);
    }

    public RepositoryScanner(GitRepositoryManager gitRepositoryManager, List<ScanRepositoryEntry> repositoryEntries, int maxRepositoriesPerScan, Path dependenciesDir) throws IOException {
        this.gitRepositoryManager = gitRepositoryManager;
        this.summary = new TestCollectionSummary(gitRepositoryManager.getRepositoryHubPath());
        this.dependenciesDir = dependenciesDir == null ? null : dependenciesDir.toAbsolutePath().normalize();
        this.maxRepositoriesPerScan = maxRepositoriesPerScan;
        if (repositoryEntries != null) {
            for (ScanRepositoryEntry entry : repositoryEntries) {
                if (!entry.isActive()) {
                    continue;
                }
                RepositoryTestInfo repoInfo = new RepositoryTestInfo(entry.getRepositoryUrl(), entry.getTeamName(), entry.getTeamCode());
                repositoryInfos.put(entry.getRepositoryUrl(), repoInfo);
            }
        }
    }
    
    /**
     * Scan a directory for Java git repositories and collect test information
     * 
     * @param rootDirectory Path to the directory containing git repositories
     * @return TestCollectionSummary containing all collected test information
     * @throws IOException if directory cannot be scanned
     */
    public TestCollectionSummary scanRepositories(boolean tempCloneMode) throws IOException {
        return scanRepositories(null, null, tempCloneMode);
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
    public TestCollectionSummary scanRepositories(List<String> includePatterns, List<String> excludePatterns, boolean tempCloneMode) throws IOException {
        Path rootPath = gitRepositoryManager.initializeRepositoryHub();
        int processedCount = 0;
        for (Map.Entry<String, RepositoryTestInfo> entry : repositoryInfos.entrySet()) {
            if (maxRepositoriesPerScan > 0 && processedCount >= maxRepositoriesPerScan) {
                System.out.println("Reached maxRepositoriesPerScan limit (" + maxRepositoriesPerScan + "), stopping further scans.");
                break;
            }
            try {
                Path repoPath = gitRepositoryManager.cloneOrUpdateRepository(entry.getKey());
                if (repoPath != null) {
                    entry.getValue().setRepositoryPath(repoPath);
                    entry.getValue().setRepositoryName(entry.getKey().substring(entry.getKey().lastIndexOf('/') + 1));
                } else {
                    System.err.println("Failed to clone or update repository " + entry.getKey());
                    continue;
                }
                System.out.println(">>>> Repository path to check: " + repoPath);
                // Check if repository path matches include/exclude patterns
                if (shouldIncludeRepository(repoPath, rootPath, includePatterns, excludePatterns)) {
                    RepositoryTestInfo repoInfo = scanRepository(entry.getValue());
                    if (repoInfo.getTotalTestClasses() > 0) {
                        summary.addRepository(repoInfo);
                    }
                } else {
                    System.out.println("Skipping repository (pattern filter): " + repoPath);
                }
            } catch (Exception e) {
                System.err.println("Error scanning repository " + entry.getKey() + ": " + e.getMessage());
                // Continue with other repositories
                e.printStackTrace();
            } finally {
                if (tempCloneMode) {
                    gitRepositoryManager.deleteRepository(entry.getKey());
                }
            }
            processedCount++;
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
     * Scan a single repository for test classes
     */
    private RepositoryTestInfo scanRepository(RepositoryTestInfo repoInfo) throws IOException {
        System.out.println("Scanning repository: " + repoInfo.getRepositoryPath());
        // Find test directories following standard Java conventions
        HashMap<String, Path> testJavaFiles = findTestJavaFiles(repoInfo.getRepositoryPath());
        System.out.println("Found " + testJavaFiles.size() + " test Java files");
        try {
            TestClassParser.configureSymbolResolver(repoInfo.getRepositoryPath(), dependenciesDir);
        } catch (IOException e) {
            System.err.println("Failed to configure symbol resolver for repository " +
                repoInfo.getRepositoryPath() + ": " + e.getMessage());
        }
        for (Path javaFile : testJavaFiles.values()) {
            try {
                ParseResult parseResult = TestClassParser.parseTestClassWithHelpers(javaFile);
                TestClassInfo testClassInfo = parseResult.getTestClassInfo();
                
                // A file is either a test file OR a helper file, not both
                // Check if we have a valid test class first
                if (testClassInfo.getTotalTestMethods() > 0) {
                    // This file contains a test class with test methods
                    repoInfo.addTestClass(testClassInfo);
                } else if (!parseResult.getHelperClasses().isEmpty()) {
                    // This file contains helper classes (non-test classes)
                    for (TestHelperClassInfo helperClass : parseResult.getHelperClasses()) {
                        repoInfo.addHelperClass(helperClass);
                    }
                }
                // else: file has neither test methods nor helper classes (shouldn't happen in test directory)
            } catch (Exception e) {
                System.err.println("Error parsing test class " + javaFile + ": " + e.getMessage());
                // Continue with other files
            }
        }
        
        return repoInfo;
    }
    
   
    /**
     * Find all Java test files following standard Java conventions
     */
    private static HashMap<String, Path> findTestJavaFiles(Path repoPath) throws IOException {
        HashMap<String, Path> testJavaFiles = new HashMap<>();
        
        Files.walkFileTree(repoPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                String fileFullPath = file.toString();
                if (fileFullPath.contains("src/test/java") || fileFullPath.contains("src\\test\\java")) {
                    if (file.toString().endsWith(".java")) {
                        testJavaFiles.put(file.toString(), file);
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
        
        return testJavaFiles;
    }
    
    
}
