package com.example.annotationextractor;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.List;

/**
 * Test class for path pattern filtering functionality
 */
public class PathPatternFilteringTest {

    @Test
    public void testBasicPatternMatching() {
        // Test basic wildcard patterns
        assertTrue("Simple wildcard should match", matchesPattern("project_a", "project_*"));
        assertTrue("Simple wildcard should match", matchesPattern("project_b", "project_*"));
        assertFalse("Simple wildcard should not match", matchesPattern("other_project", "project_*"));
        
        // Test double wildcard (matches across path separators)
        assertTrue("Double wildcard should match", matchesPattern("group/finance/project", "**/finance/*"));
        assertTrue("Double wildcard should match", matchesPattern("deep/nested/group/finance/project", "**/finance/*"));
        assertFalse("Double wildcard should not match", matchesPattern("group/other/project", "**/finance/*"));
    }

    @Test
    public void testFinanceProjectPatterns() {
        // Test the specific patterns mentioned in the user query
        String[] includePatterns = {
            "**/repository_group_finance_*/sub_project_for_view",
            "**/repository_group_finance_*/sub_project_for_dao"
        };
        
        // Test matching paths
        assertTrue("Finance view project should match", 
            matchesPattern("repository_group_finance_2024/sub_project_for_view", includePatterns[0]));
        assertTrue("Finance DAO project should match", 
            matchesPattern("repository_group_finance_2024/sub_project_for_dao", includePatterns[1]));
        assertTrue("Finance legacy project should match", 
            matchesPattern("repository_group_finance_legacy/sub_project_for_view", includePatterns[0]));
        
        // Test non-matching paths
        assertFalse("Other group should not match", 
            matchesPattern("repository_group_other/sub_project_for_view", includePatterns[0]));
        assertFalse("Other project type should not match", 
            matchesPattern("repository_group_finance_2024/sub_project_for_service", includePatterns[0]));
    }

    @Test
    public void testComplexPatterns() {
        // Test complex nested patterns
        assertTrue("Complex nested pattern should match", 
            matchesPattern("my_repository_hub/repository_group_finance_2024/sub_project_for_view", 
                         "**/repository_group_finance_*/sub_project_for_view"));
        
        assertTrue("Complex nested pattern should match", 
            matchesPattern("my_repository_hub/repository_group_finance_legacy/sub_project_for_dao", 
                         "**/repository_group_finance_*/sub_project_for_dao"));
        
        // Test with different repository hub names
        assertTrue("Different hub name should match", 
            matchesPattern("abc/repository_group_finance_2024/sub_project_for_view", 
                         "**/repository_group_finance_*/sub_project_for_view"));
    }

    @Test
    public void testExcludePatterns() {
        // Test exclude patterns
        String excludePattern = "**/expired_project*";
        
        assertTrue("Expired project should match exclude pattern", 
            matchesPattern("expired_project_2023", excludePattern));
        assertTrue("Expired project should match exclude pattern", 
            matchesPattern("group/expired_project_legacy", excludePattern));
        
        assertFalse("Active project should not match exclude pattern", 
            matchesPattern("active_project_2024", excludePattern));
    }

    @Test
    public void testMixedPatterns() {
        // Test mixed include/exclude scenarios
        String includePattern = "**/repository_group_finance_*/sub_project_for_view";
        String excludePattern = "**/expired_project*";
        
        // Test include pattern
        assertTrue("Finance view project should match include", 
            matchesPattern("repository_group_finance_2024/sub_project_for_view", includePattern));
        
        // Test exclude pattern
        assertTrue("Expired project should match exclude", 
            matchesPattern("expired_project_2023", excludePattern));
        
        // Test that both patterns work independently
        assertFalse("Other project should not match either", 
            matchesPattern("other_group/service_project", includePattern));
    }

    @Test
    public void testSpecialCharacters() {
        // Test patterns with special characters
        assertTrue("Pattern with dots should match", 
            matchesPattern("project.v1.0", "project.v*"));
        assertTrue("Pattern with underscores should match", 
            matchesPattern("project_name", "project_*"));
        assertTrue("Pattern with hyphens should match", 
            matchesPattern("project-name", "project-*"));
    }

    @Test
    public void testEmptyAndNullPatterns() {
        // Test with empty patterns (should match everything)
        assertTrue("Empty pattern should match everything", matchesPattern("any/path", ""));
        assertTrue("Null pattern should match everything", matchesPattern("any/path", null));
    }

    // Helper method to test pattern matching (simplified version for testing)
    private boolean matchesPattern(String path, String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return true;
        }
        
        // Convert glob pattern to regex (simplified version)
        String regexPattern = convertGlobToRegex(pattern);
        return path.matches(regexPattern);
    }
    
    // Simplified glob to regex converter for testing
    private String convertGlobToRegex(String globPattern) {
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
        
        // Special handling for ** at the beginning - allow it to match zero path segments
        String result = regex.toString();
        if (result.startsWith("^.*/")) {
            result = result.replaceFirst("^\\^\\.\\*/", "^.*(/)?");
        }
        
        return result;
    }
}