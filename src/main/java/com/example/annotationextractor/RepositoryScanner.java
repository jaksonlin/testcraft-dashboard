package com.example.annotationextractor;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * Scanner class to find Java git repositories and their test directories
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
        Path rootPath = Paths.get(rootDirectory);
        if (!Files.exists(rootPath) || !Files.isDirectory(rootPath)) {
            throw new IOException("Directory does not exist or is not a directory: " + rootDirectory);
        }
        
        TestCollectionSummary summary = new TestCollectionSummary(rootDirectory);
        
        // Find all git repositories in the root directory
        List<Path> gitRepositories = findGitRepositories(rootPath);
        
        for (Path repoPath : gitRepositories) {
            try {
                RepositoryTestInfo repoInfo = scanRepository(repoPath);
                if (repoInfo.getTotalTestClasses() > 0) {
                    summary.addRepository(repoInfo);
                }
            } catch (Exception e) {
                System.err.println("Error scanning repository " + repoPath + ": " + e.getMessage());
                // Continue with other repositories
            }
        }
        
        return summary;
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
